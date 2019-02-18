# SignFace
使用Face++实现课堂人脸签到的app

该app分为三部分：
* 教师使用的app
* 学生使用的app(需要切换默认的MainActivity.java)
* 用于交换教师app与学生app的数据库

app使用Face++人工智能开发平台的接口进行人脸识别 ( https://www.faceplusplus.com.cn )。具体的ApI接口为：
* Face Detect API - 用于对人脸进行特征识别，并获取独一无二的脸部识别码。
* Face Search API - 脸部照片后能够对于已预先保存的脸部集合进行匹配相似度。
