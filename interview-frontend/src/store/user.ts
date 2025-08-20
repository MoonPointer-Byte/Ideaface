import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

interface UserInfo {
  id: number;
  username: string;
  email: string;
  avatar?: string;
}

const AUTH_STORAGE_KEY = 'ai-interview-auth';

interface AuthData {
  token: string;
  user: UserInfo;
}

function saveAuthToStorage(authData: AuthData) {
  try {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authData));
  } catch (error) {
    console.error('保存认证信息到localStorage失败:', error);
  }
}

function loadAuthFromStorage(): AuthData | null {
  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  } catch (error) {
    console.error('从localStorage读取认证信息失败:', error);
    return null;
  }
}

function clearAuthFromStorage() {
  try {
    localStorage.removeItem(AUTH_STORAGE_KEY);
  } catch (error) {
    console.error('清除localStorage认证信息失败:', error);
  }
}


const defaultAvatarSeeds = [
  'Gizmo', 'Luna', 'Rocky', 'Cleo', 'Felix',
  'Misty', 'Leo', 'Zoe', 'Oscar', 'Ruby'
];

export const useUserStore = defineStore('user', () => {
 
    const authData = loadAuthFromStorage();
    const currentUser = ref<UserInfo | null>(authData?.user || null);
    const token = ref<string | null>(authData?.token || null);
  
    const isLoggedIn = computed(() => !!currentUser.value && !!token.value);
    

    const userAvatarUrl = computed(() => {
        if (currentUser.value?.avatar) {
            return currentUser.value.avatar;
        }

        if (currentUser.value?.id) {
    
            const seedIndex = currentUser.value.id % defaultAvatarSeeds.length;
            const seed = defaultAvatarSeeds[seedIndex];
            return `https://api.multiavatar.com/${seed}.svg`;
        }

    
        return `https://api.multiavatar.com/default-placeholder.svg`;
    });
  
    function setAuth(loginResponse: {token: string, user: UserInfo}) {
        currentUser.value = loginResponse.user;
        token.value = loginResponse.token;
        saveAuthToStorage(loginResponse);
        console.log('认证信息已保存:', loginResponse.user.username);
    }

    function clearAuth() {
        currentUser.value = null;
        token.value = null;
        clearAuthFromStorage();
        console.log('认证信息已清除');
    }

    function getToken(): string | null {
        return token.value;
    }
    
    return { 
        currentUser, 
        token,
        isLoggedIn, 
        userAvatarUrl, 
        setAuth, 
        clearAuth,
        getToken
    };
});