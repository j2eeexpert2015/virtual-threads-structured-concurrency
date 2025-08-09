## 👤 About the Instructor

[![Ayan Dutta - Instructor](https://img-c.udemycdn.com/user/200_H/5007784_d6b8.jpg)](https://www.udemy.com/user/ayandutta/)

Hi, I’m **Ayan Dutta**, a Software Architect, Instructor, and Content Creator.  
I create practical, hands-on courses on **Java, Spring Boot, Debugging, Git, Python**, and more.

---

## 🌐 Connect With Me

- 💬 Slack Group: [Join Here](https://join.slack.com/t/learningfromexp/shared_invite/zt-1fnksxgd0-_jOdmIq2voEeMtoindhWrA)
- 📢 After joining, go to the #java-virtual-threads-and-structured-concurrency channel
- 📧 Email: j2eeexpert2015@gmail.com
- 🔗 YouTube: [LearningFromExperience](https://www.youtube.com/@learningfromexperience)
- 📝 Medium Blog: [@mrayandutta](https://medium.com/@mrayandutta)
- 💼 LinkedIn: [Ayan Dutta](https://www.linkedin.com/in/ayan-dutta-a41091b/)

---

## 📺 Subscribe on YouTube

[![YouTube](https://img.shields.io/badge/Watch%20on%20YouTube-FF0000?style=for-the-badge&logo=youtube&logoColor=white)](https://www.youtube.com/@learningfromexperience)

---

## 📚 Explore My Udemy Courses

### 🧩 Java Debugging & Productivity

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
      <a href="https://www.udemy.com/course/intellij-idea-tips-tricks-boost-your-java-productivity">
        <img src="https://img-c.udemycdn.com/course/480x270/6180669_7726.jpg" width="250"><br/>
        <b>IntelliJ IDEA Tips & Tricks</b>
      </a>
    </td>
  </tr>
</table>

---

## 🔍 Working with `jdk.internal.vm.Continuation` in IntelliJ IDEA

`jdk.internal.vm.Continuation` is an **internal JDK API** used in Project Loom to implement virtual threads.  
Since it’s in an internal package, you **must** allow access via VM options before you can run or debug code using it.

---

### 📦 Step 1: Add VM Options
In **Run/Debug Configuration → VM Options**, add:

```text
--add-exports java.base/jdk.internal.vm=ALL-UNNAMED
--add-opens java.base/jdk.internal.vm=ALL-UNNAMED
--enable-preview
```

> **Note:**
> - `--enable-preview` is only required for JDK 19/20.
> - On JDK 21+, virtual threads are stable, so you can omit it unless you’re using other preview features.

---

### 🐞 Step 2: Add Additional Command Line Parameters for Debugging
In IntelliJ, open **Run/Debug Configurations** → **Modify options** → **Add additional command line parameters** and paste the same options:

```text
--enable-preview --add-exports java.base/jdk.internal.vm=ALL-UNNAMED --add-opens java.base/jdk.internal.vm=ALL-UNNAMED
```

This ensures the IntelliJ **debugger** can also access and step into `Continuation` methods.

---
