# 项目已更换
项目已使用kotlin重写，请移步于我的新仓库[TimePlan](https://github.com/985892345/TimePlan.git "TimePlan")

## TimePlanning软件介绍文档  
本软件是一个用于规划时间的软件，采用了滑动选取任务的功能。由于时间原因，目前只有选取任务、编辑任务的功能，还未加入进行提醒等其他的操作，设置也没有写好。  

### 一、软件介绍  
---
#### 1、日历  
由于接口请求有次数限制，所以只会加载当前周和下一周的数据并保存在本地  
###### 功能：
![日历介绍图](https://github.com/985892345/TimePlanning/blob/main/app/src/main/res/drawable-v24/img_explain_week.jpg)
> ①点击即可跳转之当日任务  
> ②左右滑动可查看附近周数

#### 2、任务操作
最外部是ViewPager2，里面的是自定义ScrollView，包含其他自定义View，结构如下  
![自定义View的结构图](https://github.com/985892345/TimePlanning/blob/main/app/src/main/res/drawable-v24/img_github_1.jpg)  
###### 功能：
![任务介绍图](https://github.com/985892345/TimePlanning/blob/main/app/src/main/res/drawable-v24/img_explain_timeview.png)
> ①长按向下滑即可选取任务  
> ②长按上下边界区可移动修改时间  
> ③长按内部区域支持整体上下移动  
> ④长按内部区域支持左右移动删除任务  
> ⑤以上操作移至皆可使时间轴自动滚动  
> ⑥点击任务即可设置任务名称和颜色  
> ⑦整体移动支持两个时间轴互相传递  

#### 3、其他界面
其他的界面还包括侧滑界面和登陆界面，这里就不细说了  

---


### 二、自定义View介绍
![自定义View的结构图](https://github.com/985892345/TimePlanning/blob/main/app/src/main/res/drawable-v24/img_github_1.jpg)  
#### 1、TimeSelectView
继承于ScrollView，主要实现了自动滑动、包含其他View的功能  
###### 其public方法如下：
> TaskBean getClickTaskBean()
>> 返回当前点击的任务数据对象  

> void refreshName()
>> 设置当前点击区域的任务名称  

> void setData(List<TaskBean> taskBeans)
>> 首次加载数据时使用  

> void setOnScrollViewListener(OnScrollViewListener l)
>> 设置滑动接口，默认所有引起的滑动都会回调滑动接口  

> void setIsOpenScrollCallBack(boolean is)
>> 设置是否在非用户触摸而调用scrollTo等方法时关闭滑动回调  

> void setTimeInterval(int timeInterval)
>> 设置时间间隔数，目前时间间隔数只用于开始设置任务时，任务的开始时间只能是间隔数的倍数  
>>（因时间问题，之后会增加结束时间也为间隔数倍数） 
 
> void setIsShowTopBottomTime(boolean is)
>> 最终的任务区域是否展示上下边界时间，即使是false在移动和改变大小时任然会展示  

> void setIsShowDifferentTime(boolean is)
>> 最终的任务区域是否展示时间差，即使是false在移动和改变大小时任然会展示  

> void setLinkViewPager2(ViewPager2 viewPager2)
>> 解决与ViewPager2的同向滑动冲突问题（传入ViewPager2，不是ViewPager）  

> void setLinkTimeSelectView(TimeSelectView linkTimeView)
>> 实现两个并排的TimeSelectView，整体移动互相传递数据  

> void setIsShowTimeLine(boolean is)
>> 设置是否显示时间线  
###### 其attrs属性如下：
> name="intervalWidth" format="dimension"
>> 左侧时间间隔的宽度  

> name="intervalHeight" format="dimension"
>> 每根水平线间隔的高度  

> name="borderColor" format="color"
>> 默认的任务边框颜色  

> name="insideColor" format="color"
>> 默认的任务内部颜色  

> name="timeTextSize" format="dimension"
>> 左侧时间轴文字大小，其他时间文字大小会依据该值进行缩小  

> name="taskTextSize" format="dimension"
>> 任务名称文字大小  

> name="centerTime" format="float"
>> 设置居中的时间，支持小数  
>> 若传入的时间处于上下边界附近无法居中的位置，则会使时间线处于顶部或尾部界面内，但不居中。若不设置，则会自动以当前时间居中。  

> name="startHour" format="integer"
>> 设置开始时间，不设置默认为2  

> name="endHour" format="integer"
>> 设置结束时间，不设置默认为明天2点  
>> 支持设置明天的时间，但请加上24，如：设置成明天的2点，就输入26  

> name="isShowTopBottomTime" format="boolean"
>> 最终的任务区域是否展示上下边界时间，即使是false在移动和改变大小时任然会展示  

> name="isShowTopBottomTime" format="boolean"
>> 最终的任务区域是否展示时间差，即使是false在移动和改变大小时任然会展示  


##### TimeSelectView中的ChildLayout
继承于FrameLayout，主要实现了整体移动的功能
##### ChildLayout中的RectView
继承于View，主要实现了任务的绘制功能
##### ChildLayout中的FrameView
继承于View，主要实现了左侧时间轴和横竖线的绘制
##### ChildLayout中的NowTimeLine
继承于View，主要实现了当前时间红线的绘制和随时间自动移动的功能
##### 实现原理讲解：
###### 一、事件处理讲解：
TimeSelectView与RectView：  
DOWN事件时，先在TimeSelectView的dispatchTouchEvent()的开启一个延时的Runnable，时间到了就是在长按，但在时间到之前必须将DOWN事件向下传递，
TimeSelectView和ChildLayout的onInterceptTouchEvent()全部return false，一直到RectView的onTouchEvent()才return true，进行拦截。
到这里时有两个问题(以下问题的解决代码可看TimeSelectView的dispatchTouchEvent()和onInterceptTouchEvent())：  

1、因为部分手机MOVE事件很敏感，一触发DOWN就马上来MOVE(怀疑是屏幕密度问题)，那么为了知道用户是否是真正的长按()，就得有一个在小范围的移动判断，
但又因为在RectView的onTouchEvent()的DOWN返回true后，MOVE和UP事件即使return false，TimeSelectView和ChildLayout的onTouchEvent()也不会
接收到MOVE事件，那小范围的移动判断就只能放到TimeSelectView中  

2、TimeSelectView继承于ScrollView，ScrollView中有一个特性，如果你的DOWN事件在自身的onTouchEvent()中不调用super处理，即使处理了MOVE，
你的ScrollVIew也不会滑动，又因为之前的DOWN事件在RectView的onTouchEvent()的DOWN中返回true，就那以后的事件不会传递给ScrollView了，对此
最好的解决办法时在ScrollView的dispatchTouchEvent()或onInterceptTouchEvent()的DOWN事件手动调用onTouchEvent()  

整个处理的思路就是开启一个延时，超过移动阈值就取消延时Runnable  
时间到之前：  
在一个移动的阈值以内，RectView处理  
移动超过阈值，说明不是长按，TimeSelectView处理  
时间到之后：
说明是长按，全部交给RectView处理

RectView与ChildLayout：  
ChildLayout主要是解决整体移动的问题，此时情况是处于延时已到，为长按的情况，所有的事件都交给了RectView处理。先在RectView中判断是否是长按，
如果是长按，则下一个MOVE事件就在ChildLayout的onInterceptTouchEvent()中拦截(这种拦截属于DOWN时return false，MOVE时return true)，之后所有的
MOVE事件就不会传递给RectView，都只在ChildLayout中处理了
> 对事件处理中，我总结出了一些规律：  
> 1、onInterceptTouchEvent()只有一次return true的机会，一旦在任意一个时间中return true，之后所有的接下来的事件都直接交给自身的onTouchEvent()
> onInterceptTouchEvent()就再也不会出现  
> 2、一旦onTouchEvent()中DOWN事件谁return true，谁就处理之后所有事件，即使你在MOVE事件中return false，也不会返回给父布局  
> 3、接2，如果你想父布局拦截某一条件的MOVE事件，可以在父布局的onInterceptTouchEvent()中return true  
> 4、接3，3有一个弊端，就是父布局的onInterceptTouchEvent()中return true后，子布局就再没有机会接收到事件，如果你想彻底的改写这些，
> 可以使用一个方法requestdisallowintercepttouchevent()用来关闭onInterceptTouchEvent()，只用dispatchTouchEvent()和onTouchEvent()来处理

#### 2、DayView
继承于View，一个简单的显示周数的View
###### 其public方法如下：
> void setDate(String[] dates)
>> 设置当周日子数  

> void setCalender(String[] calender)
>> 设置当周农历数或日子数，带有“初”、“十”、“廿”开头的会设置成白色字体显示  

> void setRectDays(String[] rectDays)
>> 设置节假日信息，只能有两种：“休”和“班”

> void setCirclePosition(int position)
>> 设置当前该显示周几，如果不在该周显示，请设置成 -1，周日到周六分别对应 0 ~ 6  

> void setMovePosition(int position)
>> 直接跳转之该位置，周日到周六分别对应 0 ~ 6  

> void setOnWeekClickListener(OnWeekClickListener l)
>> 设置点击的日期监听  
###### 其attrs属性如下：
> name="circleColor" format="color"
>> 设置园的颜色  

> name="dayTextSize" format="dimension"
>> 设置日期字体大小，其他字体随该大小改变  

### 三、心得体会
本项目是从1月27号左右开始动工，花了接近三周的时间完成自定义View————TimeSelectView，最后剩一周的时间写的其他界面和那个日历View，
本来计划是完成TimeSelectView的设置功能，结果时间不够，就这样做了基本的任务选取和修改，里面的登陆界面就是我上次第7次作业的那个界面
复制过来的，登陆接口也就是wanAndroid接口，本来想自己撸后端，结果自己太菜，再加上只有最后一周的时间，就没有弄了。  

学习了自定义View后也的确学习到了许多新的知识，比如：了解了时间分发、拦截和处理，能通过自定义View实现许多想写的界面等等。  

要优化的地方也有很多，主界面的那两个TimeSelectView就是当初想的设计，把一天分为两段，第一个TimeSelectView显示了7:00 ~ 14:00，
第二个显示14:00 ~ 21:00，上下滑动有其他时间段，所以会有一些重复的时间，就因为有重复的时间段，有一些问题也没来得及解决，但主体功能还是实现了
   
 
