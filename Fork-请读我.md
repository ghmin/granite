## 这个copy的目的
因为集群使用的db是mongodb--这个数据库的协议是 sspl的，故此fork一个出来准备对接一下mysql，pgsql等数据库。

## 如何编译部署
对于集群而已，这样做：


##### 4.3.2.4 打包Granite Cluster

首先，整个项目先：
mvn clean package install
才能执行下一步。
* **打包MgtNode**
>>```
>>cd pack/cluster-mgtnode/target
>>java -jar granite-pack-cluster-mgtnode-1.0.5-RELEASE.jar
>>```
在target目录下会看到打包好的granite-cluster-nodes-mgtnode-1.0.5-RELEASE.zip。

<br><br>
* **打包AppNode**
>>```
>>cd granite
>>cd cluster/nodes/appnode
>>mvn clean package
>>```
在target目录下会看到打包好的granite-cluster-nodes-appnode-1.0.5-RELEASE.zip和granite-cluster-nodes-appnode-1.0.5-RELEASE.tar.gz。


部署方式:
在pg上面添加一个数据库，例如，granite 然后找到两个插件的sql，分别执行：

cluster下的：
[auth.sql](cluster%2Fauth%2Fsrc%2Fmain%2Fresources%2FMETA-INF%2Fdata%2Fauth.sql)
[im.sql](cluster%2Fim%2Fsrc%2Fmain%2Fresources%2FMETA-INF%2Fdata%2Fim.sql)


执行：

mgtNode:
java -jar granite-cluster-nodes-mgtnode-1.0.5-RELEASE.jar
appNode：
java -jar granite-cluster-nodes-appnode-1.0.5-RELEASE.jar  --mgtnode-ip=192.168.50.94 --mgtnode-http-port=8090 --rt-console=true --configuration-dir=configuration --runtimes-dir=runtimes
