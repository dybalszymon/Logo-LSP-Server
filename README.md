# Logo Language Server Protocol (LSP) Implementation

Implementation of a Language Server for the Logo programming language, built using Java and LSP4J. This server provides advanced IDE features like semantic syntax highlighting, diagnostics, hover documentation, and "Go to Definition" functionality.

## Getting Started

### Prerequisites
* Java Development Kit (Amazon Corretto 20 is recommended based on the development environment).
* Gradle (included via wrapper).
* IntelliJ IDEA (2023.x or newer).
* LSP4IJ Plugin installed in IntelliJ.

### 1. Build the Server
The project uses the shadowJar plugin to create an executable "fat-jar" containing all necessary dependencies.

1. Open the project in IntelliJ or a terminal.
2. Run the following Gradle task:
   ./gradlew shadowJar
3. The generated file will be located at:
   build/libs/logo-lsp-server-1.0.0.jar

### 2. Connect to IntelliJ (LSP Client)
To use the server in IntelliJ IDEA:

1. Go to Settings -> Languages & Frameworks -> Language Servers.
2. Click + or edit your existing Logo server configuration.
3. Set Command to (adjust the path to your JDK and JAR location):
   "C:\Path\To\Your\jdk\bin\java.exe" -jar "C:\projects\lsp_logo_server\logoLspServer\build\libs\logo-lsp-server-1.0.0.jar"
4. Set Extension to: logo
5. Apply and restart the server or reopen any .logo file.

### 3. Project Structure

src/main/java/org/example/

├── Main.java                   
├── model/                      
├──-|parser/                     
│   ├── Lexer.java              
│   ├── Token.java              
│   └── LogoParser.java         
└── server/                     
    ├── LogoLangServer.java         
    ├── LogoTextDocService.java   
    └── LogoWorkspaceService.java  

### Key Technical Features

* Custom Lexer/Parser: A recursive descent parser that handles Logo's unique syntax (e.g., : for variables, " for words).
* Semantic Tokens: Implements state-of-the-art syntax highlighting. 
* Context-Aware Hover: Provides documentation for standard Logo commands with markdown formatting.
* Definition Provider (Go to definition): Supports F12 navigation. The server maintains a symbol table to jump from a procedure call to its TO ... END definition.
* Diagnostics: Real-time error reporting. Unrecognized commands or syntax errors are reported immediately to the client.

---

## Example Test Code
You can use the following snippet in a .logo file to test all features:

MAKE "SIZE 100
MAKE "ANGLE 90

TO SHAPE :SIDE :DIRECTION
REPEAT 4 [
FD :SIDE
RT :DIRECTION
]
END

CS
PU
SETXY 0 0
PD
BAD_COMMAND 50  

SHAPE :SIZE :ANGLE