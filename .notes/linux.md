## 虚拟机开启网络
```
cd /etc/sysconfig/network-scripts
ip addr
找到自己虚拟机地址的配置文件 配置网关和DNS
vi ifcfg-eth0
IPADDR=192.168.56.10
NETMASK=255.0.0.0
GATEWAY=192.168.56.1
DNS1=114.114.114.114
DNS2=8.8.8.8

重启网络 
service network restart
```
## 配置域名解析
```
vi etc/hosts
```