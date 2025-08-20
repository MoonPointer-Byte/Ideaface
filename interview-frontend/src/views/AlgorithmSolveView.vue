<template>
  <div class="solve-container">
    <div class="solve-layout">
      <!-- 左侧题目信息区域 -->
      <div class="problem-panel">
        <el-card shadow="never" class="problem-card">
          <template #header>
            <div class="problem-header">
              <h2 class="problem-title">{{ question?.title }}</h2>
              <el-tag type="info">算法题</el-tag>
            </div>
          </template>
          
          <div class="problem-content">
            <div class="section">
              <h3 class="section-title">题目描述</h3>
              <div class="section-content problem-description" v-html="parsedContent.description"></div>
            </div>
            
            <div v-if="parsedContent.dataRange" class="section">
              <h3 class="section-title">数据范围</h3>
              <div class="section-content data-range" v-html="parsedContent.dataRange"></div>
            </div>
            
            <div v-if="parsedContent.requirements" class="section">
              <h3 class="section-title">要求</h3>
              <div class="section-content requirements" v-html="parsedContent.requirements"></div>
            </div>
            
            <div v-if="parsedContent.inputFormat" class="section">
              <h3 class="section-title">输入格式</h3>
              <div class="section-content input-format" v-html="parsedContent.inputFormat"></div>
            </div>
            
            <div v-if="parsedContent.outputFormat" class="section">
              <h3 class="section-title">输出格式</h3>
              <div class="section-content output-format" v-html="parsedContent.outputFormat"></div>
            </div>
            

          </div>
        </el-card>
      </div>
      
      <!-- 右侧在线IDE区域 -->
      <div class="ide-panel">
        <el-card shadow="never" class="ide-card">
          <template #header>
            <div class="ide-header">
              <span>在线IDE</span>
              <div class="ide-controls">
                <el-select v-model="selectedLanguage" size="small" style="width: 120px; margin-right: 12px;">
                  <el-option label="JavaScript" value="javascript"></el-option>
                  <el-option label="Python" value="python"></el-option>
                  <el-option label="Java" value="java"></el-option>
                  <el-option label="C++" value="cpp"></el-option>
                </el-select>
                <el-button type="success" @click="runCode" :loading="isRunning">
                  <el-icon><VideoPlay /></el-icon>
                  运行代码
                </el-button>
                <el-button type="primary" @click="submitCode" :loading="isSubmitting">
                  提交
                </el-button>
              </div>
            </div>
          </template>
          
          <!-- IDE容器，包含编辑器和覆盖式控制台 -->
          <div class="ide-container" ref="ideContainer">
            <!-- 编辑器区域 -->
            <div class="code-editor-container">
              <!-- 编辑器工具栏 -->
              <div class="editor-toolbar">
                <div class="toolbar-left">
                  <el-tooltip content="Ctrl+S 运行代码" placement="top">
                    <el-button size="small" text @click="runCode">
                      <el-icon><VideoPlay /></el-icon>
                      运行
                    </el-button>
                  </el-tooltip>
                  <el-tooltip content="Alt+Shift+F 格式化代码" placement="top">
                    <el-button size="small" text @click="formatCode">
                      <el-icon><Setting /></el-icon>
                      格式化
                    </el-button>
                  </el-tooltip>
                </div>
                <div class="toolbar-right">
                  <span class="editor-tips">快捷键：Ctrl+S 运行 | Ctrl+Shift+Enter 提交 | Alt+Shift+F 格式化</span>
                </div>
              </div>
              
              <div ref="editorContainer" class="monaco-editor-container"></div>
            </div>
            
                      <!-- 可拖拽的分隔条 -->
          <div 
            class="resize-bar"
            @mousedown="startResize"
            @dblclick="toggleConsoleSize"
            :class="{ resizing: isResizing }"
            :style="{ 
              bottom: consoleHeight + 'px',
              zIndex: isConsoleExpanded ? 1001 : 1000
            }"
          >
            <div class="resize-handle">
              <div class="resize-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
              <div class="resize-hint">
                拖拽调整大小 | 双击{{ isConsoleExpanded ? '收起' : '展开' }}
              </div>
            </div>
          </div>
            
            <!-- 覆盖式控制台区域 -->
            <div 
              class="overlay-console" 
              :style="{ 
                height: consoleHeight + 'px',
                zIndex: isConsoleExpanded ? 1000 : 999
              }"
              :class="{ expanded: isConsoleExpanded }"
            >
            <el-tabs v-model="activeTab" class="test-tabs">
              <el-tab-pane label="测试用例" name="examples" :disabled="isRunning">
                <div v-if="examples.length > 0" class="examples-content">
                  <div v-for="(example, index) in examples" :key="index" class="example-case">
                    <h4 class="case-title">测试用例 {{ index + 1 }}</h4>
                    <div class="case-content">
                      <div class="case-input">
                        <div class="case-label">输入：</div>
                        <pre class="case-code">{{ example.input }}</pre>
                      </div>
                      <div class="case-output">
                        <div class="case-label">预期输出：</div>
                        <pre class="case-code">{{ example.output }}</pre>
                      </div>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无测试用例" />
              </el-tab-pane>
              
              <el-tab-pane label="控制台" name="console">
                <div class="console-container">
                  <!-- 控制台工具栏 -->
                  <div class="console-toolbar">
                    <div class="console-title">
                      <span>控制台</span>
                      <span v-if="isConsoleExpanded" class="expand-badge">已扩展</span>
                      <div class="console-status" :class="{ running: isRunning }">
                        <el-icon v-if="isRunning"><VideoPlay /></el-icon>
                        <span v-if="isRunning">运行中...</span>
                        <span v-else>就绪</span>
                      </div>
                    </div>
                    <div class="console-actions">
                      <el-button size="small" text @click="clearConsole">
                        清空控制台
                      </el-button>
                    </div>
                  </div>
                  
                  <!-- 控制台输出区域 -->
                  <div class="console-output" ref="consoleOutput">
                    <div 
                      v-for="(log, index) in consoleLogs" 
                      :key="index" 
                      class="console-line"
                      :class="log.type"
                    >
                      <span class="console-timestamp">{{ log.timestamp }}</span>
                      <span class="console-prefix">{{ log.prefix }}</span>
                      <span class="console-content">{{ log.content }}</span>
                    </div>
                    <div v-if="consoleLogs.length === 0" class="console-empty">
                      欢迎使用IDE控制台，点击"运行代码"执行程序
                    </div>
                  </div>
                  
                  <!-- 控制台输入区域 -->
                  <div class="console-input-section">
                    <div class="input-label">程序输入（如需输入参数，请在此处填写）：</div>
                    <el-input
                      v-model="consoleInput"
                      type="textarea"
                      :rows="3"
                      placeholder="在此输入程序需要的参数，多行输入时每行为一个参数"
                      class="console-input"
                    />
                    <div class="input-actions">
                      <el-button 
                        type="primary" 
                        size="small" 
                        @click="runCodeWithInput"
                        :loading="isRunning"
                      >
                        <el-icon><VideoPlay /></el-icon>
                        执行代码
                      </el-button>
                      <el-button size="small" @click="consoleInput = ''">
                        清空输入
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { VideoPlay, /*CircleCheck, CircleClose,*/ Setting } from '@element-plus/icons-vue';
import * as monaco from 'monaco-editor';
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker';
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker';
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker';
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker';
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker';
import api from '@/services/api';

// 配置 Monaco Editor workers
(self as any).MonacoEnvironment = {
  getWorker(_: any, label: string) {
    if (label === 'json') {
      return new jsonWorker();
    }
    if (label === 'css' || label === 'scss' || label === 'less') {
      return new cssWorker();
    }
    if (label === 'html' || label === 'handlebars' || label === 'razor') {
      return new htmlWorker();
    }
    if (label === 'typescript' || label === 'javascript') {
      return new tsWorker();
    }
    return new editorWorker();
  }
};

interface AlgorithmQuestion {
  id: number;
  title: string;
  url: string;
  description: string;
}

// interface TestResult {
//   success: boolean;
//   output?: string;
//   error?: string;
//   cases?: TestCase[];
//   executionTime?: number;
//   totalPassed?: number;
//   totalCases?: number;
// }

// interface TestCase {
//   id: number;
//   input: string;
//   expectedOutput: string;
//   actualOutput: string;
//   passed: boolean;
//   executionTime?: number;
// }

interface Example {
  input: string;
  output: string;
  explanation?: string;
}

const router = useRouter();
const route = useRoute();
const questionId = parseInt(route.params.id as string);

const question = ref<AlgorithmQuestion | null>(null);
const selectedLanguage = ref('javascript');
const isRunning = ref(false);
const isSubmitting = ref(false);
//const testResult = ref<TestResult | null>(null);
const code = ref('');
const activeTab = ref('examples'); // 'examples' 或 'console'

// 控制台相关数据
const consoleLogs = ref<Array<{
  type: 'info' | 'error' | 'success' | 'input' | 'output';
  content: string;
  timestamp: string;
  prefix: string;
}>>([]);
const consoleInput = ref('');
const consoleOutput = ref<HTMLElement>();

// 删除了算法测试相关数据

// 布局拖拽相关数据
const consoleHeight = ref(250); // 控制台高度，默认250px
const isConsoleExpanded = ref(false); // 控制台是否处于扩展状态
const isResizing = ref(false);
const startY = ref(0);
const startConsoleHeight = ref(0);
const ideContainer = ref<HTMLElement>();

// Monaco Editor相关
const editorContainer = ref<HTMLElement>();
let editor: monaco.editor.IStandaloneCodeEditor | null = null;

// 语言映射
const monacoLanguageMap: Record<string, string> = {
  javascript: 'javascript',
  python: 'python',
  java: 'java',
  cpp: 'cpp'
};

// 代码模板
const codeTemplates = {
  javascript: `// 请在这里编写你的代码
function solve() {
    // 你的解决方案
    
}

// 测试用例
console.log(solve());`,
  python: `# 请在这里编写你的代码
def solve():
    # 你的解决方案
    pass

# 测试用例
print(solve())`,
  java: `public class Solution {
    // 请在这里编写你的代码
    public static void main(String[] args) {
        Solution solution = new Solution();
        // 你的解决方案
        
    }
}`,
  cpp: `#include <iostream>
#include <vector>
using namespace std;

// 请在这里编写你的代码
int main() {
    // 你的解决方案
    
    return 0;
}`
};

// 解析题目描述中的示例
const examples = computed(() => {
  if (!question.value?.description) return [];
  
  const examples: Example[] = [];
  const description = question.value.description;
  
  // 简单的示例解析逻辑
  const exampleMatches = description.match(/示例\d+[^示例]*/g);
  if (exampleMatches) {
    exampleMatches.forEach(match => {
      const inputMatch = match.match(/输入[：:]\s*([^复制]+)/);
      const outputMatch = match.match(/返回值[：:]?\s*([^复制]+)|输出[：:]\s*([^复制]+)/);
      const explanationMatch = match.match(/说明[：:]\s*([^示例]*)/);
      
      if (inputMatch && outputMatch) {
        examples.push({
          input: inputMatch[1]?.trim() || '',
          output: (outputMatch[1] || outputMatch[2])?.trim() || '',
          explanation: explanationMatch?.[1]?.trim()
        });
      }
    });
  }
  
  return examples;
});

// 解析题目内容的不同部分
const parsedContent = computed(() => {
  if (!question.value?.description) {
    return {
      description: '',
      dataRange: '',
      requirements: '',
      inputFormat: '',
      outputFormat: ''
    };
  }
  
  let content = question.value.description;
  
  // 清理重复内容
  content = content.replace(/复制/g, '');
  content = content.replace(/输入：.*?复制.*?返回值：.*?复制/g, '');
  
  // 数学符号转换
  content = content.replace(/(\d+)\\leq/g, '$1≤');
  content = content.replace(/\\to/g, '→');
  content = content.replace(/O\(([^)]+)\)/g, '<code>O($1)</code>');
  
  // 提取各个部分
  const sections = {
    description: '',
    dataRange: '',
    requirements: '',
    inputFormat: '',
    outputFormat: ''
  };
  
  // 提取数据范围
  const dataRangeMatch = content.match(/数据范围[：:]\s*([^要求输入输出]*?)(?=要求|输入|输出|示例|$)/i);
  if (dataRangeMatch) {
    sections.dataRange = formatText(dataRangeMatch[1].trim());
    content = content.replace(dataRangeMatch[0], '');
  }
  
  // 提取要求
  const requirementsMatch = content.match(/要求[：:]\s*([^输入输出数据范围]*?)(?=输入|输出|数据范围|示例|$)/i);
  if (requirementsMatch) {
    sections.requirements = formatText(requirementsMatch[1].trim());
    content = content.replace(requirementsMatch[0], '');
  }
  
  // 提取输入格式/输入描述
  const inputMatch = content.match(/输入(?:格式|描述)?[：:]\s*([^输出返回]*?)(?=输出|返回值|示例|$)/i);
  if (inputMatch) {
    sections.inputFormat = formatText(inputMatch[1].trim());
    content = content.replace(inputMatch[0], '');
  }
  
  // 提取输出格式/返回值描述
  const outputMatch = content.match(/(?:输出|返回值)(?:格式|描述)?[：:]\s*([^示例]*?)(?=示例|$)/i);
  if (outputMatch) {
    sections.outputFormat = formatText(outputMatch[1].trim());
    content = content.replace(outputMatch[0], '');
  }
  
  // 清理剩余内容作为描述
  content = content.replace(/示例.*$/i, '');
  content = content.replace(/\s+/g, ' ').trim();
  sections.description = formatText(content);
  
  return sections;
});

// 格式化文本的辅助函数
function formatText(text: string): string {
  if (!text) return '';
  
  // 基本格式化
  text = text.replace(/\n/g, '<br>');
  text = text.replace(/\s+/g, ' ');
  
  // 数学符号美化
  text = text.replace(/≤/g, '≤');
  text = text.replace(/≥/g, '≥'); 
  text = text.replace(/→/g, '→');
  text = text.replace(/∣([^∣]+)∣/g, '|$1|');
  
  // 高亮重要信息
  text = text.replace(/空间复杂度\s*([O\(.*?\)])/g, '<strong>空间复杂度：</strong><code>$1</code>');
  text = text.replace(/时间复杂度\s*([O\(.*?\)])/g, '<strong>时间复杂度：</strong><code>$1</code>');
  text = text.replace(/进阶[：:]?\s*/g, '<strong>进阶：</strong>');
  
  // 格式化数据范围
  text = text.replace(/(\d+)\s*≤\s*([^≤\s]+)\s*≤\s*(\d+)/g, '<code>$1 ≤ $2 ≤ $3</code>');
  text = text.replace(/(\d+)\s*<\s*([^<\s]+)\s*≤\s*(\d+)/g, '<code>$1 < $2 ≤ $3</code>');
  
  return text.trim();
}

// 初始化Monaco Editor
const initEditor = () => {
  if (!editorContainer.value) return;
  
  // 配置Monaco Editor的主题和选项
  monaco.editor.defineTheme('custom-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'comment', foreground: '6A9955' },
      { token: 'keyword', foreground: '569CD6' },
      { token: 'string', foreground: 'CE9178' },
      { token: 'number', foreground: 'B5CEA8' },
    ],
    colors: {
      'editor.background': '#1e1e1e',
      'editor.foreground': '#d4d4d4',
      'editorCursor.foreground': '#aeafad',
      'editor.lineHighlightBackground': '#2a2d2e',
      'editorLineNumber.foreground': '#858585',
      'editor.selectionBackground': '#264f78',
      'editor.inactiveSelectionBackground': '#3a3d41'
    }
  });
  
  // 创建编辑器
  editor = monaco.editor.create(editorContainer.value, {
    value: code.value,
    language: monacoLanguageMap[selectedLanguage.value],
    theme: 'custom-dark',
    automaticLayout: true,
    fontSize: 14,
    fontFamily: 'Consolas, "Courier New", Monaco, Menlo, monospace',
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    folding: true,
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: false,
    cursorStyle: 'line',
    formatOnPaste: true,
    formatOnType: true,
    renderWhitespace: 'boundary',
    scrollbar: {
      vertical: 'visible',
      horizontal: 'visible',
      useShadows: false,
      verticalHasArrows: false,
      horizontalHasArrows: false
    },
    // 代码智能感知
    suggestOnTriggerCharacters: true,
    acceptSuggestionOnEnter: 'on',
    tabCompletion: 'on',
    wordBasedSuggestions: 'allDocuments',
    parameterHints: {
      enabled: true
    },
    // 错误检测和语法高亮
    renderValidationDecorations: 'on',
    // 自动补全
    quickSuggestions: {
      other: true,
      comments: true,
      strings: true
    },
    // 括号匹配
    matchBrackets: 'always',
    autoIndent: 'full',
    // 代码提示延迟
    quickSuggestionsDelay: 100,
    // 启用代码格式化
    //formatOnSave: true,
    // 滚动设置
    smoothScrolling: true,
    // 选择高亮
    //occurrencesHighlight: true,
    selectionHighlight: true,
    // 代码镜头
    codeLens: true,
    // 悬停提示
    hover: {
      enabled: true,
      delay: 300
    }
  });
  
  // 监听编辑器内容变化
  editor.onDidChangeModelContent(() => {
    if (editor) {
      code.value = editor.getValue();
    }
  });
  
  // 添加快捷键支持
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => {
    // Ctrl+S 保存并运行代码
    runCode();
  });
  
  editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.Enter, () => {
    // Ctrl+Shift+Enter 提交代码
    submitCode();
  });
  
  editor.addCommand(monaco.KeyMod.Alt | monaco.KeyMod.Shift | monaco.KeyCode.KeyF, () => {
    // Alt+Shift+F 格式化代码
    editor?.getAction('editor.action.formatDocument')?.run();
  });
  
  // 配置语言特定功能
  configureLanguageFeatures(selectedLanguage.value);
  
  // 设置编辑器高度
  editor.layout({ width: 0, height: 400 });
};

// 更新编辑器语言
const updateEditorLanguage = (language: string) => {
  if (!editor) return;
  
  const model = editor.getModel();
  if (model) {
    monaco.editor.setModelLanguage(model, monacoLanguageMap[language]);
  }
};

// 设置编辑器内容
const setEditorValue = (value: string) => {
  if (!editor) return;
  
  editor.setValue(value);
};

// 配置语言特定功能
const configureLanguageFeatures = (language: string) => {
  const monacoLang = monacoLanguageMap[language];
  
  // 注册代码片段
  monaco.languages.registerCompletionItemProvider(monacoLang, {
    provideCompletionItems: (model, position) => {
      const suggestions = getLanguageSnippets(language);
      return {
        suggestions: suggestions.map((snippet) => ({
          label: snippet.label,
          kind: monaco.languages.CompletionItemKind.Snippet,
          insertText: snippet.insertText,
          insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
          documentation: snippet.documentation,
          range: {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: model.getWordUntilPosition(position).startColumn,
            endColumn: model.getWordUntilPosition(position).endColumn
          }
        }))
      };
    }
  });
  
  // 配置代码格式化
  if (monacoLang === 'javascript') {
    monaco.languages.registerDocumentFormattingEditProvider('javascript', {
      provideDocumentFormattingEdits: (/*model*/) => {
        return [];
      }
    });
  }
};

// 获取语言特定的代码片段
const getLanguageSnippets = (language: string) => {
  const snippets: Record<string, any[]> = {
    javascript: [
      {
        label: 'for',
        insertText: 'for (let ${1:i} = 0; ${1:i} < ${2:array}.length; ${1:i}++) {\n\t${3:// body}\n}',
        documentation: 'For loop'
      },
      {
        label: 'function',
        insertText: 'function ${1:name}(${2:params}) {\n\t${3:// body}\n\treturn ${4:value};\n}',
        documentation: 'Function declaration'
      },
      {
        label: 'console.log',
        insertText: 'console.log(${1:value});',
        documentation: 'Console log'
      },
      {
        label: 'if',
        insertText: 'if (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'If statement'
      },
      {
        label: 'while',
        insertText: 'while (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'While loop'
      }
    ],
    python: [
      {
        label: 'for',
        insertText: 'for ${1:item} in ${2:iterable}:\n\t${3:pass}',
        documentation: 'For loop'
      },
      {
        label: 'def',
        insertText: 'def ${1:function_name}(${2:parameters}):\n\t${3:pass}\n\treturn ${4:value}',
        documentation: 'Function definition'
      },
      {
        label: 'if',
        insertText: 'if ${1:condition}:\n\t${2:pass}',
        documentation: 'If statement'
      },
      {
        label: 'while',
        insertText: 'while ${1:condition}:\n\t${2:pass}',
        documentation: 'While loop'
      },
      {
        label: 'class',
        insertText: 'class ${1:ClassName}:\n\tdef __init__(self${2:, parameters}):\n\t\t${3:pass}',
        documentation: 'Class definition'
      }
    ],
    java: [
      {
        label: 'for',
        insertText: 'for (${1:int i = 0}; ${2:i < length}; ${3:i++}) {\n\t${4:// body}\n}',
        documentation: 'For loop'
      },
      {
        label: 'method',
        insertText: 'public ${1:void} ${2:methodName}(${3:parameters}) {\n\t${4:// body}\n}',
        documentation: 'Method declaration'
      },
      {
        label: 'if',
        insertText: 'if (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'If statement'
      },
      {
        label: 'while',
        insertText: 'while (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'While loop'
      },
      {
        label: 'sout',
        insertText: 'System.out.println(${1:value});',
        documentation: 'System.out.println'
      }
    ],
    cpp: [
      {
        label: 'for',
        insertText: 'for (${1:int i = 0}; ${2:i < n}; ${3:i++}) {\n\t${4:// body}\n}',
        documentation: 'For loop'
      },
      {
        label: 'function',
        insertText: '${1:int} ${2:functionName}(${3:parameters}) {\n\t${4:// body}\n\treturn ${5:value};\n}',
        documentation: 'Function declaration'
      },
      {
        label: 'if',
        insertText: 'if (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'If statement'
      },
      {
        label: 'while',
        insertText: 'while (${1:condition}) {\n\t${2:// body}\n}',
        documentation: 'While loop'
      },
      {
        label: 'cout',
        insertText: 'cout << ${1:value} << endl;',
        documentation: 'Console output'
      }
    ]
  };
  
  return snippets[language] || [];
};

// 格式化代码
const formatCode = () => {
  if (!editor) return;
  
  editor.getAction('editor.action.formatDocument')?.run();
  ElMessage.success('代码格式化完成');
};

// 加载题目数据
const loadQuestion = async () => {
  try {
    const response = await fetch('/nowcoder_problems(1).json');
    const data = await response.json();
    question.value = data.find((q: AlgorithmQuestion) => q.id === questionId);
    
    if (!question.value) {
      ElMessage.error('题目不存在');
      router.push('/algorithm');
    } else {
      // 设置初始代码模板
      code.value = codeTemplates[selectedLanguage.value as keyof typeof codeTemplates];
      // 如果编辑器已初始化，更新编辑器内容
      if (editor) {
        setEditorValue(code.value);
      }
    }
  } catch (error) {
    console.error('加载题目失败:', error);
    ElMessage.error('加载题目失败');
  }
};

// 语言切换时更新代码模板
watch(selectedLanguage, (newLang) => {
  // 更新编辑器语言
  updateEditorLanguage(newLang);
  
  // 重新配置语言特定功能
  configureLanguageFeatures(newLang);
  
  // 如果当前代码是默认模板，则替换为新语言的模板
  const isDefaultTemplate = Object.values(codeTemplates).some(template => 
    code.value.trim() === template.trim()
  );
  
  if (isDefaultTemplate || !code.value.trim()) {
    const newTemplate = codeTemplates[newLang as keyof typeof codeTemplates];
    code.value = newTemplate;
    // 更新编辑器内容
    setEditorValue(newTemplate);
  }
});

// 控制台相关方法
const addToConsole = (type: 'info' | 'error' | 'success' | 'input' | 'output', content: string, prefix?: string) => {
  const now = new Date();
  const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
  
  const prefixMap = {
    info: '[INFO]',
    error: '[ERROR]',
    success: '[SUCCESS]',
    input: '[INPUT]',
    output: '[OUTPUT]'
  };
  
  consoleLogs.value.push({
    type,
    content,
    timestamp,
    prefix: prefix || prefixMap[type]
  });
  
  // 自动滚动到底部
  nextTick(() => {
    if (consoleOutput.value) {
      consoleOutput.value.scrollTop = consoleOutput.value.scrollHeight;
    }
  });
};

const clearConsole = () => {
  consoleLogs.value = [];
  addToConsole('info', '控制台已清空');
};

// 运行代码（控制台版本）
const runCodeWithInput = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码');
    return;
  }
  
  isRunning.value = true;
  
  // 切换到控制台标签页
  activeTab.value = 'console';
  
  // 记录开始执行
  addToConsole('info', '=== 程序开始执行 ===');
  
  // 如果有输入参数，显示输入内容
  if (consoleInput.value.trim()) {
    addToConsole('input', consoleInput.value.trim());
  }
  
  try {
    // 调用后端API执行代码
    const response = await api.post('/algorithm/execute', {
      code: code.value,
      language: selectedLanguage.value,
      input: consoleInput.value.trim()
    });
    
    if (response.data.success) {
      // 显示程序输出
      if (response.data.output) {
        addToConsole('output', response.data.output);
      } else {
        addToConsole('output', '(程序无输出)');
      }
      
      // 显示执行信息
      const executionTime = response.data.executionTime || 0;
      addToConsole('success', `程序执行完成，用时: ${executionTime}ms`);
      
    } else {
      // 显示错误信息
      const errorMsg = response.data.error || response.data.message || '未知错误';
      addToConsole('error', errorMsg);
    }
    
  } catch (error: any) {
    console.error('代码执行失败:', error);
    addToConsole('error', '网络错误，请检查网络连接后重试');
    ElMessage.error('代码执行失败，请稍后重试');
  } finally {
    isRunning.value = false;
    addToConsole('info', '=== 程序执行结束 ===');
  }
};

// 运行代码（原有的测试版本，保留用于提交功能）
const runCode = async () => {
  // 直接调用控制台版本的运行
  await runCodeWithInput();
};

// 检查代码行数
const checkCodeLines = (code: string): boolean => {
  // 去除空行和只包含空白字符的行
  const lines = code.split('\n').filter(line => line.trim().length > 0);
  return lines.length > 10;
};

// 检查语法错误
const checkSyntaxErrors = (code: string, language: string): string | null => {
  // 简单的语法检查
  const cleanCode = code.trim();
  
  if (language === 'javascript') {
    // 检查基本的JavaScript语法
    const openBraces = (cleanCode.match(/\{/g) || []).length;
    const closeBraces = (cleanCode.match(/\}/g) || []).length;
    if (openBraces !== closeBraces) {
      return '括号不匹配';
    }
    
    const openParens = (cleanCode.match(/\(/g) || []).length;
    const closeParens = (cleanCode.match(/\)/g) || []).length;
    if (openParens !== closeParens) {
      return '圆括号不匹配';
    }
    
    // 检查是否有基本的function定义
    if (!cleanCode.includes('function') && !cleanCode.includes('=>')) {
      return '代码中缺少函数定义';
    }
  } else if (language === 'python') {
    // 检查Python基本语法
    if (!cleanCode.includes('def ') && !cleanCode.includes('lambda')) {
      return '代码中缺少函数定义';
    }
  } else if (language === 'java') {
    // 检查Java基本语法
    const openBraces = (cleanCode.match(/\{/g) || []).length;
    const closeBraces = (cleanCode.match(/\}/g) || []).length;
    if (openBraces !== closeBraces) {
      return '括号不匹配';
    }
    
    if (!cleanCode.includes('public ') && !cleanCode.includes('private ') && !cleanCode.includes('protected ')) {
      return '代码中缺少访问修饰符';
    }
  } else if (language === 'cpp') {
    // 检查C++基本语法
    const openBraces = (cleanCode.match(/\{/g) || []).length;
    const closeBraces = (cleanCode.match(/\}/g) || []).length;
    if (openBraces !== closeBraces) {
      return '括号不匹配';
    }
    
    if (!cleanCode.includes('#include')) {
      return '代码中缺少头文件包含';
    }
  }
  
  return null; // 无语法错误
};

// 提交代码
const submitCode = async () => {
  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    // 更新统计信息（尝试次数）
    updateAttemptedStats();
    
    // 检查代码行数
    const hasEnoughLines = checkCodeLines(code.value);
    if (!hasEnoughLines) {
      // 跳转到失败界面
      router.push({
        path: `/algorithm/${questionId}/fail`,
        query: {
          errorType: '代码行数不足',
          passedCases: '0',
          totalCases: '1',
          lastInput: '代码检查',
          errorMessage: '代码行数必须大于10行，当前只有' + code.value.split('\n').filter(line => line.trim().length > 0).length + '行有效代码'
        }
      });
      return;
    }
    
    // 检查语法错误
    const syntaxError = checkSyntaxErrors(code.value, selectedLanguage.value);
    if (syntaxError) {
      // 跳转到失败界面
      router.push({
        path: `/algorithm/${questionId}/fail`,
        query: {
          errorType: '语法错误',
          passedCases: '0',
          totalCases: '1',
          lastInput: '语法检查',
          errorMessage: syntaxError
        }
      });
      return;
    }
    
    // 如果检查都通过，跳转到成功界面
    ElMessage.success('恭喜！代码检查通过！');
    
    // 更新通过统计
    const stats = JSON.parse(localStorage.getItem('algorithmStats') || '{"attempted": 0, "submitted": 0, "passed": 0}');
    stats.submitted += 1;
    stats.passed += 1;
    localStorage.setItem('algorithmStats', JSON.stringify(stats));
    
    router.push({
      path: `/algorithm/${questionId}/pass`,
      query: {
        executionTime: '120ms',
        memoryUsage: '15.2MB',
        beatPercentage: '85.6'
      }
    });
    
  } catch (error: any) {
    console.error('代码提交失败:', error);
    ElMessage.error('代码提交失败，请稍后重试');
  } finally {
    isSubmitting.value = false;
  }
};





// 更新尝试次数统计
const updateAttemptedStats = () => {
  const stats = JSON.parse(localStorage.getItem('algorithmStats') || '{"attempted": 0, "submitted": 0, "passed": 0}');
  stats.attempted += 1;
  localStorage.setItem('algorithmStats', JSON.stringify(stats));
};

// 窗口大小改变时重新调整编辑器大小
const handleResize = () => {
  if (editor) {
    nextTick(() => {
      editor?.layout();
    });
  }
  // 同时调整布局高度
  updateLayoutSizes();
};

// 更新布局尺寸
const updateLayoutSizes = () => {
  nextTick(() => {
    if (ideContainer.value) {
      const containerHeight = ideContainer.value.clientHeight;
      const maxConsoleHeight = containerHeight - 150; // 至少保留150px给编辑器
      const minConsoleHeight = 200; // 控制台最小高度
      
      // 确保控制台高度在合理范围内
      if (consoleHeight.value > maxConsoleHeight) {
        consoleHeight.value = maxConsoleHeight;
      } else if (consoleHeight.value < minConsoleHeight) {
        consoleHeight.value = minConsoleHeight;
      }
      
      // 判断是否处于扩展状态（控制台高度超过容器的40%）
      isConsoleExpanded.value = consoleHeight.value > containerHeight * 0.4;
    }
  });
};

// 开始拖拽
const startResize = (e: MouseEvent) => {
  isResizing.value = true;
  startY.value = e.clientY;
  startConsoleHeight.value = consoleHeight.value;
  
  // 添加全局事件监听
  document.addEventListener('mousemove', handleResize_Move);
  document.addEventListener('mouseup', endResize);
  
  // 防止文本选择
  document.body.style.userSelect = 'none';
  document.body.style.cursor = 'ns-resize';
  
  e.preventDefault();
};

// 拖拽过程中
const handleResize_Move = (e: MouseEvent) => {
  if (!isResizing.value || !ideContainer.value) return;
  
  const deltaY = startY.value - e.clientY; // 注意：向上拖拽为正值
  const newConsoleHeight = startConsoleHeight.value + deltaY;
  const containerHeight = ideContainer.value.clientHeight;
  
  // 设置高度限制
  const minConsoleHeight = 200; // 控制台最小高度
  const maxConsoleHeight = containerHeight - 100; // 至少保留100px给编辑器工具栏
  
  if (newConsoleHeight >= minConsoleHeight && newConsoleHeight <= maxConsoleHeight) {
    consoleHeight.value = newConsoleHeight;
    
    // 判断是否处于扩展状态
    isConsoleExpanded.value = consoleHeight.value > containerHeight * 0.4;
    
    // 实时调整编辑器大小
    if (editor) {
      nextTick(() => {
        editor?.layout();
      });
    }
  }
};

// 结束拖拽
const endResize = () => {
  isResizing.value = false;
  
  // 移除全局事件监听
  document.removeEventListener('mousemove', handleResize_Move);
  document.removeEventListener('mouseup', endResize);
  
  // 恢复样式
  document.body.style.userSelect = '';
  document.body.style.cursor = '';
  
  // 保存布局偏好到本地存储
  localStorage.setItem('ide-layout', JSON.stringify({
    consoleHeight: consoleHeight.value
  }));
};

// 从本地存储恢复布局偏好
const restoreLayout = () => {
  const saved = localStorage.getItem('ide-layout');
  if (saved) {
    try {
      const layout = JSON.parse(saved);
      if (layout.consoleHeight) {
        consoleHeight.value = layout.consoleHeight;
      }
    } catch (error) {
      console.warn('Failed to restore layout:', error);
    }
  }
};

// 双击切换控制台大小
const toggleConsoleSize = () => {
  if (!ideContainer.value) return;
  
  const containerHeight = ideContainer.value.clientHeight;
  const normalHeight = 250; // 正常高度
  const expandedHeight = containerHeight * 0.7; // 展开高度（70%）
  
  // 如果当前是展开状态，收起到正常高度；否则展开
  if (isConsoleExpanded.value) {
    consoleHeight.value = normalHeight;
    isConsoleExpanded.value = false;
  } else {
    consoleHeight.value = expandedHeight;
    isConsoleExpanded.value = true;
  }
  
  // 调整编辑器大小
  if (editor) {
    nextTick(() => {
      editor?.layout();
    });
  }
  
  // 保存偏好
  localStorage.setItem('ide-layout', JSON.stringify({
    consoleHeight: consoleHeight.value
  }));
};

onMounted(async () => {
  await loadQuestion();
  
  // 恢复布局偏好
  restoreLayout();
  
  // 等待DOM更新后初始化编辑器
  await nextTick();
  initEditor();
  
  // 初始化布局尺寸
  updateLayoutSizes();
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
  
  // 初始化控制台
  addToConsole('info', '欢迎使用IDE控制台！');
  addToConsole('info', '您可以在下方输入区域添加程序参数，然后点击"运行代码"执行程序');
});

// 组件销毁前清理编辑器
onBeforeUnmount(() => {
  if (editor) {
    editor.dispose();
    editor = null;
  }
  
  // 移除事件监听器
  window.removeEventListener('resize', handleResize);
  
  // 清理拖拽相关的事件监听器
  document.removeEventListener('mousemove', handleResize_Move);
  document.removeEventListener('mouseup', endResize);
  
  // 恢复body样式
  document.body.style.userSelect = '';
  document.body.style.cursor = '';
});
</script>

<style scoped>
.solve-container {
  height: 100vh;
  overflow: hidden;
  background-color: #f5f7fa;
}

.solve-layout {
  display: flex;
  height: 100%;
  gap: 16px;
  padding: 16px;
}

.problem-panel {
  flex: 1;
  min-width: 400px;
  max-width: 600px;
  display: flex;
  flex-direction: column;
}

.problem-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.problem-card :deep(.el-card__body) {
  background-color: #f8f9fa;
  padding: 16px;
  flex: 1;
  overflow-y: auto;
}

.ide-panel {
  flex: 1.2;
  min-width: 500px;
  display: flex;
  flex-direction: column;
}

.ide-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.ide-card :deep(.el-card__body) {
  flex: 1;
  display: block;
  padding: 16px;
  overflow: hidden;
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.problem-title {
  margin: 0;
  color: #303133;
  font-size: 20px;
}

.problem-content {
  line-height: 1.6;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding-right: 8px;
}

.section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.section-title {
  color: #409EFF;
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.section-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
}

.section-content :deep(code) {
  background-color: #f0f2f5;
  color: #e74c3c;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.section-content :deep(strong) {
  color: #303133;
  font-weight: 600;
}

.data-range {
  background-color: #f8f9ff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e6e8ff;
}

.requirements {
  background-color: #fff8e6;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #ffe6cc;
}

.input-format, .output-format {
  background-color: #f0f9ff;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #cce7ff;
}



.ide-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ide-controls {
  display: flex;
  align-items: center;
}

/* IDE容器 */
.ide-container {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.code-editor-container {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  font-size: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.editor-tips {
  color: #6c757d;
  font-size: 11px;
}

.monaco-editor-container {
  flex: 1;
  width: 100%;
  min-height: 300px;
}

.code-textarea {
  font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
  font-size: 14px;
}

.code-textarea :deep(.el-textarea__inner) {
  font-family: 'Courier New', 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.5;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border: 1px solid #3c3c3c;
}

/* 可拖拽分隔条样式 - 绝对定位，覆盖在容器上 */
.resize-bar {
  position: absolute;
  left: 0;
  right: 0;
  height: 8px;
  background: linear-gradient(to bottom, #f0f0f0, #e0e0e0);
  border-top: 1px solid #d0d0d0;
  border-bottom: 1px solid #d0d0d0;
  cursor: ns-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  user-select: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resize-bar:hover {
  background: linear-gradient(to bottom, #e8f4fd, #d1e7f0);
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3);
}

.resize-bar.resizing {
  background: linear-gradient(to bottom, #d1e7f0, #b3d9ec);
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.4);
}

.resize-handle {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  height: 100%;
  min-width: 40px;
  gap: 2px;
}

.resize-dots {
  display: flex;
  gap: 2px;
  align-items: center;
}

.resize-dots span {
  width: 3px;
  height: 3px;
  background-color: #909399;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.resize-hint {
  font-size: 10px;
  color: #909399;
  text-align: center;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.2s ease;
  user-select: none;
}

.resize-bar:hover .resize-dots span,
.resize-bar.resizing .resize-dots span {
  background-color: #409EFF;
}

.resize-bar:hover .resize-hint,
.resize-bar.resizing .resize-hint {
  opacity: 1;
  color: #409EFF;
}

/* 覆盖式控制台样式 */
.overlay-console {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  border-radius: 6px 6px 0 0;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(8px);
}

.overlay-console.expanded {
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.15), 0 -2px 8px rgba(64, 158, 255, 0.1);
  border-color: #409EFF;
  background-color: rgba(255, 255, 255, 0.98);
}

/* 为覆盖状态添加遮罩效果 */
.overlay-console.expanded::before {
  content: '';
  position: absolute;
  top: -1000px;
  left: 0;
  right: 0;
  height: 1000px;
  background: linear-gradient(to bottom, transparent, rgba(0, 0, 0, 0.02));
  pointer-events: none;
}

.test-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.test-tabs :deep(.el-tabs__header) {
  margin: 0;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e4e7ed;
  border-radius: 4px 4px 0 0;
}

.test-tabs :deep(.el-tabs__content) {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.test-tabs :deep(.el-tab-pane) {
  height: 100%;
  overflow-y: auto;
}

/* 测试用例样式 */
.examples-content {
  height: 100%;
  overflow-y: auto;
}

.example-case {
  margin-bottom: 16px;
  padding: 16px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.case-title {
  margin: 0 0 12px 0;
  color: #409EFF;
  font-size: 15px;
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.case-content {
  display: grid;
  gap: 12px;
}

.case-input, .case-output {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.case-label {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.case-code {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  margin: 0;
  border: 1px solid #e4e7ed;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #2c3e50;
  overflow-x: auto;
}

/* 控制台样式 */
.console-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border-radius: 6px;
  overflow: hidden;
}

.console-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #2d2d30;
  border-bottom: 1px solid #3e3e42;
  font-size: 12px;
}

.console-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-weight: 600;
  color: #cccccc;
}

.expand-badge {
  font-size: 10px;
  padding: 2px 6px;
  background-color: #409EFF;
  color: #ffffff;
  border-radius: 10px;
  font-weight: 500;
}

.console-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #888888;
}

.console-status.running {
  color: #4CAF50;
}

.console-actions {
  display: flex;
  align-items: center;
}

.console-output {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.4;
  background-color: #1e1e1e;
  max-height: 300px;
}

.console-line {
  margin-bottom: 4px;
  word-break: break-all;
}

.console-line.info {
  color: #d4d4d4;
}

.console-line.error {
  color: #f44747;
}

.console-line.success {
  color: #4CAF50;
}

.console-line.input {
  color: #569cd6;
}

.console-line.output {
  color: #ce9178;
}

.console-timestamp {
  color: #6a6a6a;
  margin-right: 8px;
  font-size: 11px;
}

.console-prefix {
  margin-right: 8px;
  font-weight: 600;
}

.console-content {
  white-space: pre-wrap;
}

.console-empty {
  color: #6a6a6a;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.console-input-section {
  background-color: #252526;
  border-top: 1px solid #3e3e42;
  padding: 12px;
}

.input-label {
  color: #cccccc;
  font-size: 12px;
  margin-bottom: 8px;
  font-weight: 500;
}

.console-input :deep(.el-textarea__inner) {
  background-color: #1e1e1e;
  border: 1px solid #3e3e42;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  resize: vertical;
}

.console-input :deep(.el-textarea__inner):focus {
  border-color: #007acc;
  box-shadow: 0 0 0 2px rgba(0, 122, 204, 0.2);
}

.console-input :deep(.el-textarea__inner)::placeholder {
  color: #6a6a6a;
}

.input-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

/* 删除了算法测试相关样式 */

/* 测试结果样式 */
.results-content {
  height: 100%;
  overflow-y: auto;
  padding: 8px;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .solve-layout {
    flex-direction: column;
    height: auto;
    min-height: 100vh;
  }
  
  .problem-panel, .ide-panel {
    flex: none;
    min-width: unset;
    max-width: unset;
  }
  
  .problem-panel {
    height: 400px;
  }
  
  .ide-panel {
    height: 600px;
  }
  
  .problem-content {
    max-height: 300px;
  }
  
  /* 在小屏幕上调整布局 */
  .ide-container {
    flex-direction: column;
  }
  
  .overlay-console {
    position: relative;
    height: 300px !important;
    border-radius: 4px;
    margin-top: 8px;
  }
  
  .resize-bar {
    display: none;
  }
}

@media (max-width: 768px) {
  .solve-layout {
    padding: 8px;
    gap: 8px;
  }
  
  .problem-panel {
    height: 300px;
  }
  
  .ide-panel {
    height: 500px;
  }
  
  .code-editor-container {
    min-height: 200px;
    max-height: 300px;
  }
  
  .test-section {
    min-height: 150px;
  }
}

.result-summary {
  padding: 16px;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.result-status.success .status-text {
  color: #67C23A;
  font-weight: 600;
  font-size: 16px;
}

.result-status.error .status-text {
  color: #F56C6C;
  font-weight: 600;
  font-size: 16px;
}

.execution-time {
  color: #909399;
  font-size: 14px;
  margin-left: auto;
}

.test-stats {
  color: #606266;
  font-size: 14px;
}

.test-cases {
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.case-tabs {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.case-tab {
  flex: 1;
  padding: 12px 16px;
  text-align: center;
  border-right: 1px solid #e4e7ed;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.case-tab:last-child {
  border-right: none;
}

.case-tab.active {
  background-color: #ffffff;
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
  font-weight: 600;
}

.case-tab.passed {
  background-color: #f0f9ff;
}

.case-tab.failed {
  background-color: #fef0f0;
}

.case-detail {
  padding: 16px;
}

.case-section {
  margin-bottom: 16px;
}

.section-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
  margin-bottom: 8px;
}

.section-content {
  background-color: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  margin: 0;
  border: 1px solid #e4e7ed;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: #2c3e50;
  overflow-x: auto;
}

.error-section {
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 16px;
  margin-top: 16px;
}

.error-title {
  color: #F56C6C;
}

.error-content {
  background-color: #fef0f0;
  color: #F56C6C;
  padding: 12px;
  border-radius: 6px;
  margin: 8px 0 0 0;
  border: 1px solid #fbc4c4;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .solve-layout {
    flex-direction: column;
    overflow-y: auto;
  }
  
  .problem-panel, .ide-panel {
    min-width: auto;
    max-width: none;
  }
  
  .problem-panel {
    height: auto;
    min-height: 300px;
  }
  
  .ide-panel {
    height: 600px;
  }
}
</style>