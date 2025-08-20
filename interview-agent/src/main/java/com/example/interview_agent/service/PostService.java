package com.example.interview_agent.service;

import com.example.interview_agent.dto.CategoryDto;
import com.example.interview_agent.dto.PostDto;
import com.example.interview_agent.dto.TagDto;
import com.example.interview_agent.entity.*;
import com.example.interview_agent.repository.*;
import com.example.interview_agent.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;

    @Transactional
    public PostDto createPost(PostDto postDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        Post post = new Post();
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setUser(currentUser);

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类未找到"));
            post.setCategory(category);
        }

        if (postDto.getTagNames() != null && !postDto.getTagNames().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : postDto.getTagNames()) {
                Tag tag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            post.setTags(tags);
        }

        Post savedPost = postRepository.save(post);
        return convertToDtoWithContent(savedPost);
    }

    /**
     * 【【【新增】】】: 更新文章
     */
    @Transactional
    public PostDto updatePost(Long postId, PostDto postDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章未找到: " + postId));

        // 权限校验：确保当前用户是文章的作者
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("无权修改此文章");
        }

        // 更新字段
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());

        if (postDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(postDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类未找到"));
            post.setCategory(category);
        }

        if (postDto.getTagNames() != null) {
            // 注意：直接修改集合可能导致JPA效率问题，但对于博客编辑场景可接受
            post.getTags().clear(); // 清空旧标签
            if (!postDto.getTagNames().isEmpty()) {
                Set<Tag> tags = new HashSet<>();
                for (String tagName : postDto.getTagNames()) {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                            });
                    tags.add(tag);
                }
                post.setTags(tags);
            }
        }

        Post updatedPost = postRepository.save(post);
        return convertToDtoWithContent(updatedPost);
    }

    /**
     * 【【【新增】】】: 删除文章
     */
    @Transactional
    public void deletePost(Long postId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = userDetails.getId();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章未找到: " + postId));

        // 权限校验：确保当前用户是文章的作者
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("无权删除此文章");
        }

        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto> getAllPosts(Pageable pageable, Long categoryId, String tagName) {
        Specification<Post> spec = (root, query, criteriaBuilder) -> {
            if (categoryId != null) {
                return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
            }
            if (tagName != null && !tagName.isEmpty()) {
                return criteriaBuilder.equal(root.join("tags").get("name"), tagName);
            }
            return criteriaBuilder.conjunction();
        };
        return postRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    @Transactional
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章未找到: " + id));
        post.setViewCount(post.getViewCount() + 1);
        Post updatedPost = postRepository.save(post);
        return convertToDtoWithContent(updatedPost);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long postId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("文章未找到: " + postId);
        }

        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);

        boolean isLiked;
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            postRepository.decrementLikeCount(postId);
            isLiked = false;
        } else {
            User userReference = userRepository.getReferenceById(userId);
            Post postReference = postRepository.getReferenceById(postId);

            PostLike newLike = new PostLike();
            newLike.setUser(userReference);
            newLike.setPost(postReference);
            postLikeRepository.save(newLike);

            postRepository.incrementLikeCount(postId);
            isLiked = true;
        }

        postRepository.flush();
        int newLikeCount = postRepository.findById(postId).map(Post::getLikeCount).orElse(0);

        return Map.of("likeCount", newLikeCount, "isLiked", isLiked);
    }

    @Transactional(readOnly = true)
    public List<PostDto> getHotPosts(int limit, String sortBy) {
        Pageable topN = PageRequest.of(0, limit);
        List<Post> hotPosts;

        if ("likes".equalsIgnoreCase(sortBy)) {
            hotPosts = postRepository.findAllByOrderByLikeCountDesc(topN);
        } else {
            hotPosts = postRepository.findHotPosts(topN);
        }

        return hotPosts.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private PostDto convertToDto(Post post) {
        PostDto dto = convertBase(post);
        return dto;
    }

    private PostDto convertToDtoWithContent(Post post) {
        PostDto dto = convertBase(post);
        dto.setContent(post.getContent());
        return dto;
    }

    private PostDto convertBase(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setViewCount(post.getViewCount());
        dto.setLikeCount(post.getLikeCount());
        dto.setCreateTime(post.getCreateTime());
        dto.setUpdateTime(post.getUpdateTime());

        PostDto.UserInfo authorInfo = new PostDto.UserInfo();
        authorInfo.setId(post.getUser().getId());
        authorInfo.setUsername(post.getUser().getUsername());
        dto.setAuthor(authorInfo);

        if (post.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(post.getCategory().getId());
            categoryDto.setName(post.getCategory().getName());
            categoryDto.setSlug(post.getCategory().getSlug());
            dto.setCategory(categoryDto);
        }

        if (post.getTags() != null && !post.getTags().isEmpty()) {
            Set<TagDto> tagDtos = post.getTags().stream().map(tag -> {
                TagDto tagDto = new TagDto();
                tagDto.setId(tag.getId());
                tagDto.setName(tag.getName());
                return tagDto;
            }).collect(Collectors.toSet());
            dto.setTags(tagDtos);
        }

        return dto;
    }
    /**
     * 【【【新增】】】: 搜索文章的业务逻辑
     * @param keyword 搜索关键词
     * @param pageable 分页信息
     * @return DTO格式的分页结果
     */
    @Transactional(readOnly = true)
    public Page<PostDto> searchPosts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            // 如果关键词为空，可以返回空结果或者所有文章的第一页
            return Page.empty(pageable);
        }
        Page<Post> posts = postRepository.searchByTitleOrContent(keyword, pageable);
        return posts.map(this::convertToDto);
    }

    /**
     * 【【【新增】】】: 根据用户ID获取文章分页列表
     */
    @Transactional(readOnly = true)
    public Page<PostDto> getPostsByUserId(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserId(userId, pageable);
        return posts.map(this::convertToDto); // 复用不含content的转换方法
    }
}