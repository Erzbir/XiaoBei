# 介绍:

这是一个基于HTTP多线程体温上报脚本, 希望有朋友能优化其中的代码, 非常感谢!

每个用户的打卡包括 验证/登录/上报/保存日志/推送消息, 每个用户的操作是一个线程, 之前做过一个把所有操作分成线程的版本,
速度会提高很多, 但是有太多线程异常就选择了这个安全稳定的做法

联系方式: 2118675437@qq.com

博客链接: erzbir.com

# 使用:

GitHub Action实现自动运行或者在电脑上设置定时任务启动脚本

GitHub Action的文件已经写好, 不需要自己创建, 在.github目录下

$不是对应学校的无法登录, 但是你可以仿造报文来运行$

使用非常简单

1. fork本仓库到自己仓库, fork按钮在页面右上角, 再将代码clone到本地.
2. 获取location待会配置文件中会用到, 获取网址: https://api.xiaobaibk.com/api/map/
3. 配置名称为"config.json"的配置文件, 将所有信息填进去; head和user都支持添加多个
4. 如果想使用环境变量来打卡而不用创建新仓库请忽略此步操作, 看下面的补充说明. 配置完后提交到github脚本就已经就绪了.
   注意提交时请修改".git"文件夹中的"config"文件,
   将远程仓库地址改到你的一个私有仓库(因为会涉及到隐私问题)
5. 如果不会提交github, 找个教程看就好

$补充说明$:
> 如果你不想创建私人仓库, 可以用在fork下来的本仓库 Settings->Secrets->Action中, 设置Action的环境变量
>
> 做法: 将整个配置文件内容添加到Action环境变量, 环境名称名为英文名, 将Main类中第28行注释掉并取消29行的注释(
> 跟着Main类中29行末尾的注释来)
> 
> *如果使用环境变量则不要取消System.out()方法的注释(隐私问题)*

有什么不会的操作可以通过最上面的联系方式找到我

# 配置文件说明

user中除了place都是必填(但place标签必须有)

place可以自动获取, 也可以自己填(建议自动获取)

user中的email是收件邮箱, sender_email是发发件邮箱

key是你邮箱的服务, 需要自行去邮箱官网获取, host是服务的域名

如果你不懂请求头, 请尽量不要改head中除"user-agent"的其他内容, 只修改"user-agent"(请求头)即可

user-agent随便搜索一个填上就行, 最好填手机的吧

```json
{
  "head": [
    {
      "user-agent": "iPhone13,3(iOS/14.4) Uninview(Uninview/1.0.0) Weex/0.26.0 1125x2436",
      "accept": "*/*",
      "accept-language": "zh-cn",
      "accept-encoding": "gzip, deflate, br",
      "content-type": "application/json;charset=UTF-8"
    }
  ],
  "user": [
    {
      "username": "3490181804214",
      "password": "42424",
      "location": "103.340138,29.905536",
      "place": "中国-四川省-成都市-成华区",
      "email": "111111111@qq.com"
    }
  ],
  "sender_email": "1111111111@qq.com",
  "key": "dwoafifbajslajd",
  "host": "smtp.qq.com"
}

```

# 推送通知:

这里只写了邮件推送, 要是有人完善telegramBot和server酱就更好了

可以通过推送通知来确认打卡情况

如果发邮件过多smtp服务器Keynote会禁止访问, 这会导致线程阻塞, 如果推送卡住, 不要惊慌, 推送超时时间设置的是3s

比如你用的QQ邮箱

1. 搜索"如何获取QQ邮箱smtp服务码"
2. 将服务码填到"key"后面
3. 将"smtp.qq.com"填到"host"后面
4. "sender_email"后面填服务码对应的邮箱
5. 将每个user成员中的"email"后面填写收件邮箱

# 查看失败日志:

程序如果出现错误会生成一个"ErrLog.log"的文件, 可以在里面看到为什么失败

# Bug:

暂未发现

