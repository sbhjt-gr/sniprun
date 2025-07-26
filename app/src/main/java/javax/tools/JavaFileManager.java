package javax.tools;

/**
 * Minimal implementation for Android compatibility with Eclipse JDT
 */
public interface JavaFileManager {
    
    interface Location {
        String getName();
        boolean isOutputLocation();
    }
    
    enum StandardLocation implements Location {
        CLASS_OUTPUT("CLASS_OUTPUT", true),
        CLASS_PATH("CLASS_PATH", false),
        SOURCE_PATH("SOURCE_PATH", false),
        ANNOTATION_PROCESSOR_PATH("ANNOTATION_PROCESSOR_PATH", false),
        PLATFORM_CLASS_PATH("PLATFORM_CLASS_PATH", false);
        
        private final String name;
        private final boolean outputLocation;
        
        StandardLocation(String name, boolean outputLocation) {
            this.name = name;
            this.outputLocation = outputLocation;
        }
        
        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public boolean isOutputLocation() {
            return outputLocation;
        }
    }
}
