# Week 01：开发环境与个人仓库

## 环境检查

### Java：

![image-20260920164956371](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20260920164956371.png)

### Maven：

![image-20260920165132173](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20260920165132173.png)

### Git:

![image-20260920165203857](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20260920165203857.png)

### docker:

![image-20260920165234330](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20260920165234330.png)

![image-20260920165252067](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20260920165252067.png)

## 概念回答

### 1. 什么是微服务架构？

微服务架构是一种将一个大型应用拆分成多个相对独立的小型服务的软件架构方式。每个服务负责特定的业务功能，可以独立开发、运行和部署，不同服务之间通过接口进行通信。

### 2. 微服务和单体架构的主要区别是什么？

单体架构通常将系统的多个功能集中在一个项目中开发和部署，而微服务架构会按照业务功能将系统拆分成多个独立服务。微服务在独立开发、部署和扩展方面更加灵活，但同时也会增加服务通信、部署和运维的复杂度。

### 3. 为什么本课程先实现单体系统，再逐步拆分为微服务？

先实现单体系统可以让我们首先理解完整的业务流程和项目结构。在掌握系统功能后再逐步进行微服务拆分，可以更加直观地理解服务为什么需要拆分、应该如何划分以及拆分后产生了哪些变化。

### 4. 为什么作业需要提供可重复运行的测试或验证脚本？

可重复运行的测试或验证脚本能够快速检查程序是否按照预期运行。当项目不断修改或者逐渐拆分成多个微服务后，可以通过这些脚本验证原有功能是否受到影响，也方便其他人复现和检查项目运行结果。