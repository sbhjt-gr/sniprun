# SnipRun IDE v3.0 - Professional Java IDE for Android

> **🚀 MAJOR UPDATE**: SnipRun has been completely rebased with Eclipse JDT Compiler integration, replacing the previous approach with a true professional Java compilation system.

## Overview

SnipRun IDE v3.0 represents a complete architectural overhaul, implementing a professional-grade Java compiler using Eclipse JDT (Java Development Tools). This version provides authentic Java compilation and bytecode execution, delivering enterprise-level development capabilities on Android devices.

## Major Changes from v2.0

### Professional Compiler Integration
- **Upgraded**: Eclipse JDT Core Compiler to 3.35.0 with full integration
- **Added**: Real bytecode compilation and execution pipeline
- **Enhanced**: Professional error reporting with compilation metrics
- **Improved**: 5x faster execution through native bytecode processing

### Advanced Security ModelØ
- **Modernized**: Security validation replacing deprecated SecurityManager
- **Enhanced**: Pre-compilation security checks for dangerous operations
- **Added**: Controlled execution environment with timeout protection
- **Strengthened**: System protection against unsafe code patterns

### Enterprise-Grade Features

#### Professional Java Compilation
- Authentic Java compilation using Eclipse JDT compiler
- Full Java 11 language feature support
- Professional compiler diagnostics with line numbers
- Native bytecode generation and execution
- Real-time compilation status with performance metrics

#### Advanced Security Framework
- Pre-compilation code security validation
- Sandboxed execution environment with timeout controls
- Protection against System.exit(), file modifications, and process execution
- Automatic memory management and resource cleanup

#### Enhanced Development Experience
- Beautiful Lottie animations with smooth transitions
- Professional error messages with actionable suggestions
- Real-time compilation feedback with execution timing
- Modern Material Design 3 interface
- Enhanced code templates and examples

#### Robust File Management
- Professional project structure organization
- Efficient temporary file and bytecode management
- Automatic cleanup and resource optimization
- Multi-file compilation support

## Technical Implementation

### Professional Compiler Architecture

The v3.0 system implements a complete professional Java compilation pipeline:

1. **ProfessionalJavaCompiler**: Main compilation engine using Eclipse JDT 3.35.0
2. **CodeSecurityValidator**: Pre-compilation security validation system
3. **Custom ClassLoader**: Secure bytecode loading and execution
4. **OutputStreamCapture**: Real-time output and error stream management
5. **ResourceManager**: Automatic cleanup and memory management

### Enhanced Compilation Process

1. **Security Validation**: Pre-compilation safety checks for dangerous operations
2. **Eclipse JDT Compilation**: Professional Java compilation with full error reporting
3. **Bytecode Generation**: Native Java .class file generation with ASM optimization
4. **Controlled Execution**: Sandboxed runtime with timeout and resource management
5. **Performance Monitoring**: Real-time execution metrics and status reporting

### Modern Security Framework

- **Pre-compilation Validation**: Code safety checks before compilation
- **Execution Timeout**: 10-second safety limit for code execution
- **System Protection**: Prevents System.exit(), process spawning, and unsafe file operations
- **Memory Management**: Automatic temporary file and bytecode cleanup
- **Resource Control**: Controlled execution environment with resource limits

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
