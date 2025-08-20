<template>
  <div ref="container" class="avatar-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

interface Props { backgroundColor?: number | string; headColor?: number | string; bodyColor?: number | string; }
const props = withDefaults(defineProps<Props>(), { backgroundColor: 0x1e1e1e, headColor: 0x77b5fe, bodyColor: 0x4a4a4a });

const container = ref<HTMLElement | null>(null);
let renderer: THREE.WebGLRenderer;
let animationFrameId: number;
let head: THREE.Group;
let onWindowResize: () => void;

onMounted(() => initScene());
onUnmounted(() => {
  cancelAnimationFrame(animationFrameId);
  if (renderer) { renderer.dispose(); renderer.forceContextLoss(); }
  window.removeEventListener('resize', onWindowResize);
});

const initScene = () => {
  if (!container.value) return;
  const scene = new THREE.Scene();
  scene.background = new THREE.Color(props.backgroundColor);
  scene.fog = new THREE.Fog(props.backgroundColor, 3, 10);
  const camera = new THREE.PerspectiveCamera(50, container.value.clientWidth / container.value.clientHeight, 0.1, 1000);
  camera.position.set(0, 1.2, 3.5);
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(container.value.clientWidth, container.value.clientHeight);
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.shadowMap.enabled = true;
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  container.value.appendChild(renderer.domElement);
  scene.add(new THREE.AmbientLight(0xffffff, 0.5));
  const dirLight = new THREE.DirectionalLight(0xffffff, 1.5);
  dirLight.position.set(5, 10, 7.5);
  dirLight.castShadow = true;
  scene.add(dirLight);
  const avatar = createProceduralAvatar();
  scene.add(avatar);
  const controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.enablePan = false;
  controls.enableZoom = false;
  controls.target.set(0, 1, 0);
  controls.minPolarAngle = Math.PI / 3;
  controls.maxPolarAngle = Math.PI / 2;
  const clock = new THREE.Clock();
  const animate = () => {
    animationFrameId = requestAnimationFrame(animate);
    const elapsedTime = clock.getElapsedTime();
    if (head) head.rotation.y = Math.sin(elapsedTime * 0.7) * 0.15;
    controls.update();
    renderer.render(scene, camera);
  };
  animate();
  onWindowResize = () => {
    if (container.value) {
      camera.aspect = container.value.clientWidth / container.value.clientHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(container.value.clientWidth, container.value.clientHeight);
    }
  };
  window.addEventListener('resize', onWindowResize);
};
const createProceduralAvatar = (): THREE.Group => {
  const group = new THREE.Group();
  const bodyMaterial = new THREE.MeshStandardMaterial({ color: props.bodyColor, roughness: 0.8, metalness: 0.2 });
  const torso = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.5, 1.2, 32), bodyMaterial);
  torso.position.y = 0.6;
  torso.castShadow = true;
  group.add(torso);
  head = new THREE.Group();
  head.position.y = 1.45;
  const headMaterial = new THREE.MeshStandardMaterial({ color: props.headColor, roughness: 0.1, metalness: 0.9, emissive: props.headColor, emissiveIntensity: 0.3 });
  const headMesh = new THREE.Mesh(new THREE.SphereGeometry(0.4, 32, 32), headMaterial);
  headMesh.castShadow = true;
  head.add(headMesh);
  const eye = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.05, 0.1), new THREE.MeshBasicMaterial({ color: 0xffffff }));
  eye.position.z = 0.38;
  head.add(eye);
  group.add(head);
  return group;
};
</script>

<style scoped>
.avatar-container { width: 100%; height: 100%; border-radius: 12px; overflow: hidden; cursor: grab; }
.avatar-container:active { cursor: grabbing; }
</style>