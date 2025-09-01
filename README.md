# 👤 About the Instructor

[![Ayan Dutta - Instructor](https://img-c.udemycdn.com/user/200_H/5007784_d6b8.jpg)](https://www.udemy.com/user/ayandutta/)

Hi, I'm **Ayan Dutta**, a Software Architect, Instructor, and Content Creator.  
I create practical, hands-on courses on **Java, Spring Boot, Debugging, Git, Python**, and more.

---

## 🌐 Connect With Me

- 💬 Slack Group: [Join Here](https://join.slack.com/t/learningfromexp/shared_invite/zt-1fnksxgd0-_jOdmIq2voEeMtoindhWrA)  
- 📢 After joining, go to the **#java-virtual-threads-and-structured-concurrency** channel  
- 📧 Email: j2eeexpert2015@gmail.com  
- 🔗 YouTube: [LearningFromExperience](https://www.youtube.com/@learningfromexperience)  
- 📝 Medium Blog: [@mrayandutta](https://medium.com/@mrayandutta)  
- 💼 LinkedIn: [Ayan Dutta](https://www.linkedin.com/in/ayan-dutta-a41091b/)

---

## 📺 Subscribe on YouTube

[![YouTube](https://img.shields.io/badge/Watch%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@learningfromexperience)

---

## 📚 Explore My Udemy Courses

### 🧩 Java Debugging Courses with Eclipse, IntelliJ IDEA, and VS Code

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/eclipse-debugging-techniques-and-tricks">
        <img src="https://img-c.udemycdn.com/course/480x270/417118_3afa_4.jpg" width="250"><br/>
        <b>Eclipse Debugging Techniques</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-intellij-idea">
        <img src="https://img-c.udemycdn.com/course/480x270/2608314_47e4.jpg" width="250"><br/>
        <b>Java Debugging With IntelliJ</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/java-debugging-with-visual-studio-code-the-ultimate-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/5029852_d692_3.jpg" width="250"><br/>
        <b>Java Debugging with VS Code</b>
      </a>
    </td>
  </tr>
</table>

---

### 💡 Java Productivity & Patterns

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/intellij-idea-tips-tricks-boost-your-java-productivity">
        <img src="https://img-c.udemycdn.com/course/480x270/6180669_7726.jpg" width="250"><br/>
        <b>IntelliJ IDEA Tips & Tricks</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/design-patterns-in-javacreational">
        <img src="https://img-c.udemycdn.com/course/480x270/779796_5770_2.jpg" width="250"><br/>
        <b>Creational Design Patterns</b>
      </a>
    </td>
  </tr>
</table>

---

### 🐍 Python Debugging Courses

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/learn-python-debugging-with-pycharm-ide">
        <img src="https://img-c.udemycdn.com/course/480x270/4840890_12a3_2.jpg" width="250"><br/>
        <b>Python Debugging With PyCharm</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/python-debugging-with-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/5029842_d36f.jpg" width="250"><br/>
        <b>Python Debugging with VS Code</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/get-started-with-python-debugging-in-visual-studio-code">
        <img src="https://img-c.udemycdn.com/course/480x270/6412275_a17d.jpg" width="250"><br/>
        <b>Python Debugging (Free)</b>
      </a>
    </td>
  </tr>
</table>

---

### 🛠 Git & GitHub Courses

<table>
  <tr>
    <td>
      <a href="https://www.udemy.com/course/getting-started-with-github-desktop">
        <img src="https://img-c.udemycdn.com/course/480x270/6112307_3b4e_2.jpg" width="250"><br/>
        <b>GitHub Desktop Guide</b>
      </a>
    </td>
    <td>
      <a href="https://www.udemy.com/course/learn-to-use-git-and-github-with-eclipse-a-complete-guide">
        <img src="https://img-c.udemycdn.com/course/480x270/3369428_995b.jpg" width="250"><br/>
        <b>Git & GitHub with Eclipse</b>
      </a>
    </td>
  </tr>
</table>

---

## 📦 Virtual Threads & Structured Concurrency Repository

This repository contains all the demo code for the Udemy course: **Java Virtual Threads & Structured Concurrency w/ Spring Boot**

### ⚙️ Requirements

- **JDK 21+** (stable virtual threads)  
- JDK 19/20 also supported with `--enable-preview`  
- **IntelliJ IDEA** or **Eclipse** (recommended)
- **Maven 3.9+** for building projects

### 📥 Getting Started

Clone the repository and navigate to the project directory:

```bash
git clone https://github.com/j2eeexpert2015/virtual-threads-structured-concurrency.git
cd virtual-threads-structured-concurrency
```

Build the project:

```bash
mvn clean compile
```

## ▶️ Running the Demos

### **From IntelliJ IDEA:**
1. Go to **Run/Debug Configurations → VM Options**
2. Add the following VM options:
   ```
   --add-exports java.base/jdk.internal.vm=ALL-UNNAMED
   --enable-preview
   ```

### **From Command Line (Maven):**

**Run Basic Virtual Thread Demo:**
```bash
mvn clean compile exec:java -Dexec.mainClass="com.example.virtualthreadcreation.BasicVirtualThreadCreation"
```

**Run Virtual Thread Builder Demo:**
```bash
mvn exec:java -Dexec.mainClass="com.example.virtualthreadcreation.VirtualThreadCreationWithBuilder"
```

**Run Structured Concurrency Demo:**
```bash
mvn exec:java -Dexec.mainClass="com.example.structuredconcurrency.ProductPageWithStructuredConcurrency"
```

**Run Continuation Demo:**
```bash
mvn exec:java -Dexec.mainClass="com.example.continuation.SimpleContinuationDemo" -Dexec.args="--add-exports java.base/jdk.internal.vm=ALL-UNNAMED --enable-preview"
```

## 🐞 Debugging Configuration

### **IntelliJ IDEA Debugging Setup:**
For debugging `jdk.internal.vm.Continuation` and other internal APIs:

1. Go to **Run/Debug Configurations**
2. Click **Modify options**
3. Select **Add additional command line parameters**
4. Add the same VM options:
   ```
   --add-exports java.base/jdk.internal.vm=ALL-UNNAMED --enable-preview
   ```

This enables the IntelliJ debugger to step into `Continuation` methods and other internal JDK classes.

## 📁 Project Structure Overview

```
src/main/java/com/example/
├── virtualthreadcreation/     # Basic virtual thread creation examples
├── structuredconcurrency/     # Structured concurrency demonstrations  
├── scopedvalue/              # ScopedValue vs ThreadLocal examples
├── continuation/             # Low-level Continuation API examples
├── pinning/                 # Virtual thread pinning detection
├── mountandunmount/         # Thread mounting behavior
└── util/                   # Utility classes (JFR, profiling, etc.)
```

## 🔧 Key Demo Categories

### 🧵 **Virtual Thread Creation**
- `BasicVirtualThreadCreation.java` - Simple VT creation patterns
- `VirtualThreadCreationWithBuilder.java` - Builder API usage
- `VirtualThreadCreationWithThreadFactory.java` - Factory patterns

### 🏗️ **Structured Concurrency** 
- `ProductPageWithStructuredConcurrency.java` - Real-world scenarios
- `ProductAvailabilityWithShutdownOnSuccess.java` - Success patterns
- `StructuredVsUnstructuredDemo.java` - Side-by-side comparison

### 🔄 **Context Propagation**
- `ScopedValueExample.java` - Modern context handling
- `ThreadLocalInheritanceProblem.java` - Legacy issues demonstration

### ⚡ **Performance & Monitoring**
- `VirtualThreadMountUnmount.java` - Mount/unmount behavior
- `VirtualThreadPinning.java` - Pinning detection and prevention
- `MillionVirtualThreads.java` - Scalability demonstrations

## 📝 Important Notes

> **JDK Version Compatibility:**
> - `--enable-preview` is **only required** for JDK 19/20
> - On **JDK 21+**, virtual threads are stable - you can omit `--enable-preview` unless using other preview features
> - **Structured Concurrency** requires `--enable-preview` on **all JDK versions** (still in preview as of JDK 21)

> **Internal API Access:**
> - `--add-exports java.base/jdk.internal.vm=ALL-UNNAMED` is **required** for `Continuation` examples
> - This exports internal JDK APIs that are normally encapsulated by the module system
> - ⚠️ **Warning:** Internal APIs can change without notice - use only for learning purposes

## 🚨 Troubleshooting

**Issue:** `IllegalAccessError` with Continuation examples  
**Solution:** Ensure VM options include `--add-exports java.base/jdk.internal.vm=ALL-UNNAMED`

**Issue:** `ClassNotFoundException` for preview features  
**Solution:** Add `--enable-preview` flag and ensure using compatible JDK version

**Issue:** Maven exec plugin not finding main class  
**Solution:** Run `mvn clean compile` first to ensure classes are built

## 🎯 Course Companion Repository

This repository is designed to complement the **"Java Virtual Threads & Structured Concurrency w/ Spring Boot"** Udemy course. Each demo corresponds to specific course sections and lectures for hands-on learning.

For production Spring Boot examples with monitoring and observability, check out the companion repository: **[sample-retail-app](https://github.com/j2eeexpert2015/sample-retail-app)**

---

**Happy Learning! 🚀**
