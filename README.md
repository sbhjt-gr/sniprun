# SnipRun IDE v2.0 - Professional Java IDE for Android

## Overview

SnipRun IDE has been completely revamped to use a professional Java compiler approach inspired by Cosmic IDE. The app now features a real Eclipse JDT (Java Development Tools) compiler that provides true Java compilation and execution capabilities on Android.

## Major Changes from v1.0

### Compiler Architecture
- **Removed**: BeanShell-based script execution
- **Added**: Eclipse JDT Core Compiler 3.35.0
- **Added**: ASM bytecode manipulation library 9.6
- **Added**: Professional Java compilation pipeline

### New Features

#### Professional Java Compiler
- Real Java compilation using Eclipse JDT compiler
- Support for Java 11 language features
- Proper error reporting with line numbers
- Bytecode generation and execution

#### Security & Safety
- Sandboxed execution environment
- Restricted security manager preventing harmful operations
- System.exit() blocking
- File system access restrictions

#### Enhanced Error Reporting
- Formatted compiler error messages
- Helpful tips for common programming errors
- Line-by-line error indication
- Professional diagnostic output

#### Improved User Interface
- Rich output formatting with symbols
- Professional compilation status indicators
- Detailed compilation and execution logs
- Enhanced error visualization

#### Advanced File Management
- Project-based file organization
- Real Java file compilation
- Multiple file support
- Improved file tree navigation

## Technical Implementation

### Cosmic IDE Approach Integration

The new compiler system is inspired by Cosmic IDE's architecture:

1. **JavaCompiler**: Main compilation engine using Eclipse JDT
2. **MemoryClassLoader**: Custom class loader for compiled bytecode
3. **CaptureSystemStreams**: Output capture system
4. **CompilerErrorFormatter**: Professional error message formatting
5. **RestrictiveSecurityManager**: Security sandbox implementation

### Compilation Process

1. **Source Code Analysis**: Automatic class detection and wrapping
2. **Eclipse JDT Compilation**: Professional Java compilation
3. **Bytecode Generation**: Standard .class file generation
4. **Secure Execution**: Sandboxed runtime environment
5. **Output Capture**: Real-time output streaming

### Security Features

- **System.exit() Prevention**: Blocks application termination
- **File System Protection**: Prevents unauthorized file operations
- **Runtime Restrictions**: Controlled execution environment
- **Memory Management**: Automatic cleanup and resource management

## File Storage

Java source files and compiled bytecode are stored in:
```
/data/data/com.gorai.sniprun/files/
├── projects/          # User project files
│   └── src/          # Java source files
└── compiler/         # Compiler working directory
    ├── classes/      # Compiled .class files
    └── temp/         # Temporary compilation files
```

## Dependencies

### Core Libraries
- **Eclipse JDT Core**: `org.eclipse.jdt:ecj:3.35.0`
- **ASM Bytecode Manipulation**: `org.ow2.asm:asm:9.6`
- **ASM Tree API**: `org.ow2.asm:asm-tree:9.6`
- **ASM Utilities**: `org.ow2.asm:asm-util:9.6`

### Android Components
- **Android Support Libraries**: Latest versions
- **Material Design Components**: Enhanced UI
- **RecyclerView**: Advanced file tree display

## Usage Examples

### Simple Hello World
```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### Advanced Example with Collections
```java
import java.util.*;

public class CollectionsDemo {
    public static void main(String[] args) {
        List<String> languages = new ArrayList<>();
        languages.add("Java");
        languages.add("Kotlin");
        languages.add("Python");
        
        System.out.println("Programming Languages:");
        for (String lang : languages) {
            System.out.println("- " + lang);
        }
        
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Java", 95);
        scores.put("Kotlin", 90);
        
        scores.forEach((lang, score) -> 
            System.out.println(lang + ": " + score));
    }
}
```

## Error Handling

The new system provides comprehensive error handling:

### Compilation Errors
- Syntax errors with line numbers
- Type checking errors
- Missing import statements
- Method signature mismatches

### Runtime Errors
- Exception handling with stack traces
- Security violations
- Memory-related issues
- I/O operation failures

## Performance Improvements

- **Faster Compilation**: Eclipse JDT is optimized for performance
- **Better Memory Management**: Automatic cleanup of compiled classes
- **Efficient Execution**: Direct bytecode execution vs. interpretation
- **Reduced Overhead**: Minimal runtime dependencies

## Future Enhancements

### Planned Features
- **Multi-file Projects**: Support for complex project structures
- **External Dependencies**: JAR file import capability
- **Debugging Support**: Breakpoints and step-through debugging
- **Code Completion**: IntelliSense-style code assistance
- **Syntax Highlighting**: Enhanced editor with syntax coloring

### Potential Integrations
- **Kotlin Support**: Kotlin compiler integration
- **Gradle Build System**: Build script support
- **Version Control**: Git integration
- **Cloud Sync**: Project synchronization

## Credits

This implementation is inspired by and builds upon the excellent work of the Cosmic IDE team:
- **Cosmic IDE**: https://github.com/Cosmic-Ide/Cosmic-IDE
- **Eclipse JDT**: Eclipse Java Development Tools
- **ASM Library**: ObjectWeb ASM bytecode manipulation framework

## License

This project maintains its original licensing while incorporating open-source components under their respective licenses.
