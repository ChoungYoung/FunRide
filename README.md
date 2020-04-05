README For English Version: [README-EN](https://github.com/ChoungYoung/FunRide/blob/master/README-EN.MD "README-EN")

# FunRide
快乐出行是使用kotlin开发的一款导航应用

## 主要功能

在进入APP的时候会请求用户定位权限，获取不到地图信息的时候引导用户打开GPS进行定位

用户点击出发后会开启一个服务计算用户的使用时长和行走路线距离，并随着用户移动规划最适合路线，在使用过程中GPS信号弱会提示用户

行程结束展示用户的路径图、花费时长和路线距离

语言做了英语和汉语两个版本。

## 使用到的库

该项目基于百度地图SDK开发，使用的百度地图的定位和地图功能，开发过程中用到了jetpack库里的**ViewModel**、**LiveData**和**Room**

Room数据库搭配kotlin协程一起使用，保存本次行程路线供后续显示使用，界面布局使用的**ConstraintLayout**，减少了页面层级，页面由fragment展示，共用ViewModel数据
