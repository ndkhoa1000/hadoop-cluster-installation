🌐 [English](./README.md) | Tiếng Việt

# Hướng Dẫn Cài Đặt Cụm Apache Hadoop

Hướng dẫn toàn diện này sẽ hướng dẫn bạn cài đặt và cấu hình cụm Apache Hadoop, bao gồm cả thiết lập single-node (pseudo-distributed) và multi-node (fully distributed).

## Mục Lục

1. [Yêu Cầu Tiên Quyết](#yêu-cầu-tiên-quyết)
2. [Tải Xuống và Cài Đặt](#tải-xuống-và-cài-đặt)
3. [Cấu Hình Môi Trường](#cấu-hình-môi-trường)
4. [Cấu Hình Hadoop](#cấu-hình-hadoop)
5. [Thiết Lập Single-Node (Pseudo-Distributed)](#thiết-lập-single-node)
6. [Thiết Lập Multi-Node (Fully Distributed)](#thiết-lập-multi-node)
7. [Khởi Động Cụm](#khởi-động-cụm)
8. [Xác Minh và Kiểm Tra](#xác-minh-và-kiểm-tra)
9. [Các Vấn Đề Thường Gặp và Khắc Phục Sự Cố](#khắc-phục-sự-cố)
10. [Các Lệnh Hữu Ích](#các-lệnh-hữu-ích)

## Yêu Cầu Tiên Quyết

### Yêu Cầu Hệ Thống
- **Hệ Điều Hành**: Linux, hoặc Windows (với WSL)
- **Java**: OpenJDK 8, 11, hoặc 17 (khuyến nghị: OpenJDK 11)
- **Bộ Nhớ**: Tối thiểu 4GB RAM (khuyến nghị 8GB+ cho multi-node)
- **Dung Lượng Đĩa**: Tối thiểu 20GB dung lượng khả dụng
- **Mạng**: Truy cập SSH giữa các node (cho thiết lập multi-node)

### Phần Mềm Cần Thiết
- Java Development Kit (JDK)
- SSH server và client
- rsync (để đồng bộ hóa tệp)

## Tải Xuống và Cài Đặt
### Bước 0: Thiết Lập Người Dùng **master**
```bash
sudo adduser hadoop

# thêm mật khẩu
sudo passwd hadoop

# thêm hadoop vào nhóm sudo
sudo adduser hadoop sudo 

# chuyển sang người dùng hadoop
su hadoop

# Điều hướng đến thư mục home của hadoop
cd ~
```

### Bước 1: Tải Xuống Hadoop

| Phiên Bản | Ngày Phát Hành | Tải Xuống Binary |
|---------|--------------|-----------------|
| 3.4.1   | 18 Tháng 10, 2024  | [hadoop-3.4.1.tar.gz](https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz) |

```bash
# Tải xuống Hadoop 3.4.1
wget https://archive.apache.org/dist/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz

# Xác minh tải xuống (tùy chọn)
wget https://downloads.apache.org/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz.sha512

shasum -a 512 -c hadoop-3.4.1.tar.gz.sha512
```

### Bước 2: Giải Nén và Cài Đặt

```bash
# Giải nén tệp nén
tar -xzf hadoop-3.4.1.tar.gz

# Giữ nó ở vị trí ưa thích của bạn
mv hadoop-3.4.1 ~/hadoop
```

## Cấu Hình Môi Trường

### Bước 1: Cài Đặt Java (nếu chưa cài đặt)

#### Trên Ubuntu/Debian:
```bash
sudo apt update && sudo apt install openjdk-11-jdk -y
```

#### Trên CentOS/RHEL:
```bash
sudo yum install java-11-openjdk-devel
```

### Bước 2: Cấu Hình Biến Môi Trường

Thêm những dòng sau vào `~/.bashrc` hoặc `~/.profile` của bạn:

```bash
# Môi trường Java
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux

# Môi trường Hadoop
export HADOOP_HOME=$HOME/hadoop  # hoặc đường dẫn cài đặt của bạn
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME

# Thêm các tệp thực thi Hadoop vào PATH
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```

Áp dụng các thay đổi:
```bash
source ~/.bashrc  # hoặc ~/.profile
```

### Bước 3: Cấu Hình SSH (Cần thiết cho các hoạt động cụm)

```bash
# Tạo cặp khóa SSH (nếu chưa tồn tại)
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa

# Thêm khóa công khai vào authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

# Đặt quyền thích hợp
chmod 0600 ~/.ssh/authorized_keys

# Kiểm tra SSH đến localhost
ssh localhost
```

## Cấu Hình Hadoop

Điều hướng đến thư mục cấu hình Hadoop:
```bash
cd $HADOOP_HOME/etc/hadoop
```

### Bước 1: Cấu Hình `hadoop-env.sh`

```bash
# Chỉnh sửa hadoop-env.sh
vim hadoop-env.sh

# Thêm hoặc cập nhật JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

### Bước 2: Cấu Hình Các Thành Phần Cốt Lõi

#### `core-site.xml`
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
        <description>URI hệ thống tệp mặc định</description>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>$HADOOP_HOME/tmp</value>
        <description>Thư mục tạm thời cho Hadoop</description>
    </property>
</configuration>
```

#### `hdfs-site.xml`
```xml
<configuration>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>$HADOOP_HOME/data/namenode</value>
        <description>Thư mục cho metadata namenode</description>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>$HADOOP_HOME/data/datanode</value>
        <description>Thư mục cho dữ liệu datanode</description>
    </property>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
        <description>Sao chép khối mặc định</description>
    </property>
    <property>
        <name>dfs.namenode.checkpoint.dir</name>
        <value>$HADOOP_HOME/data/secondary</value>
        <description>Thư mục checkpoint secondary namenode</description>
    </property>
</configuration>
```

#### `mapred-site.xml`
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
        <description>Tên framework MapReduce</description>
    </property>
    <property>
        <name>mapreduce.application.classpath</name>
        <value>$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/*:$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/lib/*</value>
    </property>
</configuration>
```

#### `yarn-site.xml`
```xml
<configuration>
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>localhost</value>
        <description>Hostname ResourceManager</description>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
        <description>Dịch vụ phụ trợ cho NodeManager</description>
    </property>
    <property>
        <name>yarn.nodemanager.env-whitelist</name>
        <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
    </property>
</configuration>
```

### Bước 3: Tạo Các Thư Mục Cần Thiết

```bash
# Tạo thư mục dữ liệu
sudo mkdir -p $HADOOP_HOME/data/{namenode,datanode,secondary}
sudo mkdir -p $HADOOP_HOME/tmp

# Đặt quyền sở hữu thích hợp
sudo chown -R $USER:$USER $HADOOP_HOME/data
sudo chown -R $USER:$USER $HADOOP_HOME/tmp
```

## Thiết Lập Single-Node

### Định Dạng NameNode (Chỉ thiết lập lần đầu)

```bash
hdfs namenode -format
```

### Khởi Động Dịch Vụ Hadoop

```bash
# Khởi động HDFS
start-dfs.sh

# Khởi động YARN
start-yarn.sh

# Hoặc khởi động tất cả dịch vụ cùng lúc
start-all.sh
```

### Xác Minh Cài Đặt

```bash
# Kiểm tra các tiến trình đang chạy
jps

# Đầu ra dự kiến nên bao gồm:
# - NameNode
# - DataNode
# - ResourceManager
# - NodeManager
# - SecondaryNameNode
```

## Thiết Lập Multi-Node

### Yêu Cầu Tiên Quyết cho Multi-Node

1. Nhiều máy có cài đặt Hadoop
2. Kết nối mạng giữa tất cả các node
3. Truy cập SSH từ master đến tất cả các node slave
4. Cùng tên người dùng trên tất cả các node
5. Thời gian đồng bộ trên tất cả các node

### Bước 1: Cấu Hình Master Node

#### Cập nhật `core-site.xml` trên master:
```xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://master:9000</value>
</property>
```

#### Cập nhật `yarn-site.xml` trên master:
```xml
<property>
    <name>yarn.resourcemanager.hostname</name>
    <value>master</value>
</property>
```

#### Cấu hình tệp workers:
```bash
# Chỉnh sửa $HADOOP_HOME/etc/hadoop/workers
vim $HADOOP_HOME/etc/hadoop/workers

# Thêm tên host của các worker node (một node mỗi dòng)
# Xóa localhost cho cụm (nếu có)
node1
node2
node3
```

### Bước 1.5: Cấu Hình Phân Bổ Bộ Nhớ
Phân bổ bộ nhớ có thể khó khăn trên các node RAM thấp vì các giá trị mặc định không phù hợp với các node có ít hơn 8GB RAM. Phần này sẽ làm nổi bật cách phân bổ bộ nhớ hoạt động cho các công việc MapReduce và cung cấp cấu hình mẫu cho các node **2GB RAM**.

1. Cập nhật `yarn-site.xml`:
```xml
<property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>1536</value>
</property>

<property>
        <name>yarn.scheduler.maximum-allocation-mb</name>
        <value>1536</value>
</property>

<property>
        <name>yarn.scheduler.minimum-allocation-mb</name>
        <value>128</value>
</property>

<property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
</property>
```
2. Cập nhật `mapred-site.xml`:
```xml
<property>
        <name>yarn.app.mapreduce.am.resource.mb</name>
        <value>512</value>
</property>

<property>
        <name>mapreduce.map.memory.mb</name>
        <value>256</value>
</property>
<property>
        <name>mapreduce.reduce.memory.mb</name>
        <value>256</value>
</property>
```

### Bước 2: Cấu Hình Slave Node

1. Sao chép toàn bộ cấu hình Hadoop từ master đến tất cả các slave node:
```bash
scp -r $HADOOP_HOME user@worker-node:~/
```

2. Cập nhật `hdfs-site.xml` trên slaves để trỏ đến master:
```xml
<property>
    <name>dfs.namenode.name.dir</name>
    <value>$HADOOP_HOME/data/namenode</value>
</property>
```

### Bước 3: Cấu Hình Mạng

Cập nhật `/etc/hosts` trên **tất cả các node**:
```bash
# Thêm các mục cho tất cả các node
192.168.1.100   master
192.168.1.101   node1
192.168.1.102   node2
192.168.1.103   node3
```

### Bước 4: Khởi Động Cụm Multi-Node

Từ master node:
```bash
# Định dạng namenode (chỉ lần đầu tiên)
hdfs namenode -format

# Khởi động cụm
start-all.sh
```

## Khởi Động và Dừng Cụm Hadoop

### 🎯 **Khởi Động Nhanh (Khuyến nghị, sử dụng script được viết trong repository Github này)**

Sử dụng script điều khiển tiện lợi được bao gồm trong repository này:

```bash
# Khởi động cụm
./hadoop-control.sh start

# Dừng cụm  
./hadoop-control.sh stop

# Khởi động lại cụm
./hadoop-control.sh restart

# Kiểm tra trạng thái cụm
./hadoop-control.sh status

# Hiển thị trợ giúp
./hadoop-control.sh help
```

Script này cung cấp:
- ✅ **Đầu ra có màu** để dễ đọc
- ✅ **Kiểm tra lỗi tự động** và xác thực
- ✅ **Phát hiện dịch vụ thông minh** - biết những gì đang chạy
- ✅ **Liên kết giao diện web** khi cụm hoạt động tốt
- ✅ **Quy trình khởi động/dừng an toàn** với trình tự thích hợp

### 🚀 Quản Lý Dịch Vụ Thủ Công

#### Phương pháp 1: Khởi Động Tất Cả Dịch Vụ Cùng Lúc

```bash
# Khởi động tất cả dịch vụ Hadoop
start-all.sh

# Kiểm tra xem tất cả dịch vụ có đang chạy không
jps
```

#### Phương pháp 2: Khởi Động Dịch Vụ Riêng Lẻ

```bash
# 1. Khởi động dịch vụ HDFS (NameNode, DataNode, SecondaryNameNode)
start-dfs.sh

# 2. Khởi động dịch vụ YARN (ResourceManager, NodeManager)
start-yarn.sh

# 3. Khởi động MapReduce Job History Server (tùy chọn)
mapred --daemon start historyserver
```

#### Phương pháp 3: Khởi Động Dịch Vụ Từng Cái Một

```bash
# Khởi động NameNode
hdfs --daemon start namenode

# Khởi động DataNode
hdfs --daemon start datanode

# Khởi động SecondaryNameNode
hdfs --daemon start secondarynamenode

# Khởi động ResourceManager
yarn --daemon start resourcemanager

# Khởi động NodeManager
yarn --daemon start nodemanager

# Khởi động Job History Server
mapred --daemon start historyserver
```

### ⏹️ Dừng Dịch Vụ Hadoop

#### Dừng Tất Cả Dịch Vụ

```bash
# Dừng tất cả dịch vụ Hadoop
stop-all.sh
```

#### Dừng Dịch Vụ Riêng Lẻ

```bash
# Dừng dịch vụ YARN
stop-yarn.sh

# Dừng dịch vụ HDFS
stop-dfs.sh

# Dừng Job History Server
mapred --daemon stop historyserver
```

#### Dừng Dịch Vụ Từng Cái Một

```bash
# Dừng Job History Server
mapred --daemon stop historyserver

# Dừng NodeManager
yarn --daemon stop nodemanager

# Dừng ResourceManager
yarn --daemon stop resourcemanager

# Dừng SecondaryNameNode
hdfs --daemon stop secondarynamenode

# Dừng DataNode
hdfs --daemon stop datanode

# Dừng NameNode
hdfs --daemon stop namenode
```

### 🔄 Khởi Động Lại Dịch Vụ

```bash
# Khởi động lại tất cả dịch vụ
stop-all.sh && start-all.sh

# Khởi động lại chỉ HDFS
stop-dfs.sh && start-dfs.sh

# Khởi động lại chỉ YARN
stop-yarn.sh && start-yarn.sh
```

### ✅ Xác Minh Dịch Vụ Đang Chạy

#### Kiểm Tra Các Tiến Trình Java Đang Chạy

```bash
# Liệt kê tất cả các tiến trình Java liên quan đến Hadoop
jps

# Đầu ra dự kiến nên bao gồm:
# 12345 NameNode
# 12346 DataNode
# 12347 SecondaryNameNode
# 12348 ResourceManager
# 12349 NodeManager
# 12350 JobHistoryServer (nếu được khởi động)
```

#### Kiểm Tra Trạng Thái Dịch Vụ Cụ Thể

```bash
# Kiểm tra xem NameNode có đang chạy không
hdfs dfsadmin -report

# Kiểm tra xem YARN có đang chạy không
yarn node -list

# Kiểm tra sức khỏe cụm
hdfs dfsadmin -safemode get
```

### 🧪 Kiểm Tra Cài Đặt Hadoop

#### Test 1: Các Thao Tác HDFS Cơ Bản

```bash
# Tạo thư mục người dùng của bạn trong HDFS
hdfs dfs -mkdir -p /user/$USER

# Tạo thư mục kiểm tra
hdfs dfs -mkdir /user/$USER/test

# Liệt kê thư mục gốc HDFS
hdfs dfs -ls /

# Liệt kê thư mục người dùng của bạn
hdfs dfs -ls /user/$USER

# Kiểm tra sức khỏe HDFS
hdfs fsck /
```

#### Test 2: Tải Lên và Tải Xuống Tệp

```bash
# Tạo tệp kiểm tra cục bộ
echo "Xin chào Thế giới Hadoop!" > test.txt
echo "Đây là tệp kiểm tra cho Hadoop HDFS" >> test.txt

# Tải tệp lên HDFS
hdfs dfs -put test.txt /user/$USER/

# Liệt kê tệp trong HDFS
hdfs dfs -ls /user/$USER/

# Xem nội dung tệp trong HDFS
hdfs dfs -cat /user/$USER/test.txt

# Tải xuống tệp từ HDFS
hdfs dfs -get /user/$USER/test.txt downloaded_test.txt

# Xác minh tệp đã tải xuống
cat downloaded_test.txt

# Dọn dẹp tệp kiểm tra
rm test.txt downloaded_test.txt
hdfs dfs -rm /user/$USER/test.txt
```

#### Test 3: Chạy Công Việc MapReduce Mẫu

```bash
# Tạo thư mục đầu vào cho MapReduce
hdfs dfs -mkdir /input

# Sao chép tệp cấu hình Hadoop làm đầu vào
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml /input/

# Chạy ví dụ đếm từ
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar wordcount /input /output

# Kiểm tra đầu ra
hdfs dfs -ls /output/
hdfs dfs -cat /output/part-r-00000 | head -20

# Dọn dẹp
hdfs dfs -rm -r /output
hdfs dfs -rm -r /input
```

#### Test 4: Kiểm Tra Ứng Dụng YARN

```bash
# Chạy ứng dụng YARN đơn giản (tính toán Pi)
yarn jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar pi 2 100

# Kiểm tra lịch sử ứng dụng
yarn application -list -appStates ALL
```

#### Test 5: Kiểm Tra Benchmark Hiệu Suất

```bash
# Test Ghi TestDFSIO
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -write -nrFiles 4 -fileSize 128MB

# Test Đọc TestDFSIO
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -read -nrFiles 4 -fileSize 128MB

# Dọn dẹp dữ liệu kiểm tra
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -clean
```

### 📊 Giám Sát và Kiểm Tra Sức Khỏe

#### Truy Cập Giao Diện Web

```bash
# Mở giao diện web (chạy các lệnh này để lấy URL)
echo "NameNode Web UI: http://localhost:9870"
echo "ResourceManager Web UI: http://localhost:8088"
echo "Job History Server: http://localhost:19888"
echo "DataNode Web UI: http://localhost:9864"
echo "NodeManager Web UI: http://localhost:8042"
```

#### Giám Sát Dòng Lệnh

```bash
# Kiểm tra tóm tắt cụm
hdfs dfsadmin -report

# Kiểm tra hệ thống tệp
hdfs fsck /

# Giám sát ứng dụng YARN
yarn top

# Kiểm tra trạng thái node
yarn node -list -showDetails

# Xem metrics cụm
yarn cluster -lnl
```

## 🔧 Quản Lý Node Cụm

### Script Quản Lý Node

Tôi đã tạo script toàn diện để quản lý các node cụm:

```bash
# Làm cho script có thể thực thi (nếu chưa)
chmod +x ./manage-cluster-nodes.sh

# Hiển thị tất cả các lệnh có sẵn
./manage-cluster-nodes.sh help
```

### Các Tính Năng Chính

#### 📋 **Thông Tin Node**
```bash
# Liệt kê tất cả các node hiện tại
./manage-cluster-nodes.sh list

# Hiển thị trạng thái cụm chi tiết
./manage-cluster-nodes.sh status
```

#### ➕ **Thêm Node**
```bash
# Thêm worker node mới
./manage-cluster-nodes.sh add node1

# Thêm node theo địa chỉ IP
./manage-cluster-nodes.sh add 192.168.1.100
```

#### ➖ **Xóa Node (Quy Trình An Toàn)**
```bash
# Bước 1: Ngừng hoạt động node một cách an toàn
./manage-cluster-nodes.sh decommission node1

# Bước 2: Xóa khỏi cụm (sau khi ngừng hoạt động hoàn thành)
./manage-cluster-nodes.sh remove node1
```

#### 🔄 **Quản Lý Node**
```bash
# Đưa lại node đã ngừng hoạt động
./manage-cluster-nodes.sh recommission node1
```

#### 🏗️ **Chuyển Đổi Cụm**
```bash
# Chuyển đổi thiết lập single-node sang multi-node
./manage-cluster-nodes.sh convert-multi

# Chuyển đổi multi-node trở lại single-node
./manage-cluster-nodes.sh convert-single
```

#### 💾 **Sao Lưu và Khôi Phục Cấu Hình**
```bash
# Sao lưu cấu hình hiện tại
./manage-cluster-nodes.sh backup

# Khôi phục từ sao lưu
./manage-cluster-nodes.sh restore
```

### Thêm Worker Node Mới - Quy Trình Hoàn Chỉnh

1. **Thêm node vào cấu hình cụm:**
   ```bash
   ./manage-cluster-nodes.sh add node2
   ```

2. **Thiết lập truy cập SSH không cần mật khẩu:**
   ```bash
   # Sao chép khóa SSH đến node mới
   ssh-copy-id user@node2
   
   # Kiểm tra truy cập SSH
   ssh node2
   ```

3. **Cài đặt Hadoop trên node mới:**
   ```bash
   # Sao chép cài đặt Hadoop đến node mới
   scp -r $HADOOP_HOME user@node2:~/
   ```

4. **Sao chép tệp cấu hình:**
   ```bash
   # Sao chép cấu hình đến node mới
   scp -r $HADOOP_HOME/etc/hadoop/* user@node2:$HADOOP_HOME/etc/hadoop/
   ```

5. **Làm mới các node cụm:**
   ```bash
   # Làm mới node YARN
   yarn rmadmin -refreshNodes
   
   # Làm mới node HDFS
   hdfs dfsadmin -refreshNodes
   ```

6. **Khởi động dịch vụ trên node mới:**
   ```bash
   # Trên worker node mới, khởi động DataNode và NodeManager
   ssh node2 "$HADOOP_HOME/bin/hdfs --daemon start datanode"
   ssh node2 "$HADOOP_HOME/bin/yarn --daemon start nodemanager"
   ```

### Xóa Node Một Cách An Toàn - Quy Trình Hoàn Chỉnh

1. **Ngừng hoạt động node:**
   ```bash
   ./manage-cluster-nodes.sh decommission node2
   ```

2. **Giám sát tiến trình ngừng hoạt động:**
   ```bash
   # Kiểm tra trạng thái ngừng hoạt động HDFS
   hdfs dfsadmin -report
   
   # Kiểm tra trạng thái node YARN
   yarn node -list -all
   ```

3. **Chờ ngừng hoạt động hoàn thành** (các khối dữ liệu được chuyển đến các node khác)

4. **Xóa node:**
   ```bash
   ./manage-cluster-nodes.sh remove node2
   ```

5. **Dừng dịch vụ trên node đã xóa:**
   ```bash
   ssh node2 "$HADOOP_HOME/bin/yarn --daemon stop nodemanager"
   ssh node2 "$HADOOP_HOME/bin/hdfs --daemon stop datanode"
   ```

### Tệp Cấu Hình Được Sửa Đổi

Script tự động quản lý các tệp này:
- `$HADOOP_HOME/etc/hadoop/workers` - Danh sách các worker node
- `$HADOOP_HOME/etc/hadoop/core-site.xml` - Cấu hình cốt lõi
- `$HADOOP_HOME/etc/hadoop/yarn-site.xml` - Cấu hình YARN  
- `$HADOOP_HOME/etc/hadoop/dfs.exclude` - Danh sách ngừng hoạt động HDFS
- `$HADOOP_HOME/etc/hadoop/yarn.exclude` - Danh sách ngừng hoạt động YARN

### Sao Lưu và Khôi Phục

Tất cả các thay đổi cấu hình được tự động sao lưu vào:
```
$HADOOP_HOME/backups/hadoop_config_YYYYMMDD_HHMMSS.tar.gz
```

Bạn có thể khôi phục bất kỳ bản sao lưu nào bằng cách sử dụng:
```bash
./manage-cluster-nodes.sh restore
```

### Tham Khảo Nhanh

Để có hướng dẫn tham khảo nhanh hoàn chỉnh, xem: `CLUSTER-MANAGEMENT.md`

---
## Xác Minh và Kiểm Tra

### Kiểm Tra Trạng Thái Cụm

```bash
# Kiểm tra trạng thái HDFS
hdfs dfsadmin -report

# Kiểm tra node YARN
yarn node -list

# Kiểm tra các tiến trình đang chạy
jps
```

### Giao Diện Web

- **HDFS NameNode**: http://localhost:9870
- **YARN ResourceManager**: http://localhost:8088
- **MapReduce Job History**: http://localhost:19888

### Các Thao Tác HDFS Cơ Bản

```bash
# Tạo thư mục trong HDFS
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/$USER

# Liệt kê nội dung HDFS
hdfs dfs -ls /

# Sao chép tệp đến HDFS
hdfs dfs -put /path/to/local/file /user/$USER/

# Sao chép tệp từ HDFS
hdfs dfs -get /user/$USER/file /path/to/local/
```

### Chạy Công Việc MapReduce Mẫu

```bash
# Tạo thư mục đầu vào
hdfs dfs -mkdir /input

# Sao chép tệp đầu vào
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml /input

# Chạy ví dụ đếm từ
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.4.1.jar wordcount /input /output

# Kiểm tra đầu ra
hdfs dfs -cat /output/part-r-00000
```

## Khắc Phục Sự Cố

### Các Vấn Đề Thường Gặp và Giải Pháp

#### 0. Đồng bộ tệp nhanh sử dụng **rsync**:
Sử dụng ***scp*** có thể không đồng bộ tệp trên tất cả các node. Vậy ***rsync*** sẽ ánh xạ và thay thế tất cả các tệp có cùng tên và đảm bảo chúng sẽ được cập nhật chính xác.
```bash
rsync -avz ~/hadoop/etc/hadoop/ user@node1:~/hadoop/etc/hadoop/
```

#### 1. Lỗi Liên Quan Đến Java
```bash
# Xác minh cài đặt Java
java -version
echo $JAVA_HOME

# Đảm bảo JAVA_HOME được thiết lập trong hadoop-env.sh
```

#### 2. Vấn Đề Kết Nối SSH
```bash
# Kiểm tra kết nối SSH
ssh localhost
ssh node1

# Kiểm tra thiết lập khóa SSH
ls -la ~/.ssh/
```

#### 3. Lỗi Từ Chối Quyền
```bash
# Sửa quyền thư mục
sudo chown -R $USER:$USER $HADOOP_HOME
chmod 755 $HADOOP_HOME/data/*
```

#### 4. Cổng Đã Được Sử Dụng
```bash
# Kiểm tra xem cái gì đang sử dụng cổng
netstat -tulpn | grep :9000
lsof -i :9000

# Kết thúc tiến trình nếu cần thiết
kill -9 <PID>
```

#### 5. DataNode Không Khởi Động
```bash
# Kiểm tra logs
tail -f $HADOOP_HOME/logs/hadoop-*-datanode-*.log

# Giải pháp phổ biến: Xóa và định dạng lại
stop-all.sh
rm -rf $HADOOP_HOME/data/datanode/*
hdfs namenode -format -force
start-all.sh
```

### Vị Trí Tệp Log

```bash
# Thư mục logs Hadoop
$HADOOP_HOME/logs/

# Các tệp log quan trọng
hadoop-*-namenode-*.log
hadoop-*-datanode-*.log
yarn-*-resourcemanager-*.log
yarn-*-nodemanager-*.log
```

## Các Lệnh Hữu Ích

### Lệnh HDFS
```bash
# Kiểm tra hệ thống tệp
hdfs fsck /

# Các thao tác safe mode
hdfs dfsadmin -safemode leave
hdfs dfsadmin -safemode enter

# Cân bằng cụm
hdfs balancer

# Ngừng hoạt động node
hdfs dfsadmin -refreshNodes
```

### Lệnh YARN
```bash
# Liệt kê ứng dụng
yarn application -list

# Kết thúc ứng dụng
yarn application -kill <application_id>

# Quản lý node
yarn node -list -all
yarn rmadmin -refreshNodes
```

### Quản Trị Cụm
```bash
# Kiểm tra sức khỏe cụm
hdfs dfsadmin -report
yarn node -list -showDetails

# Giám sát cụm
hadoop dfsadmin -printTopology
yarn top
```

### Giám Sát Hiệu Suất
```bash
# Kiểm tra sử dụng đĩa
hdfs dfs -du -h /

# Giám sát tài nguyên hệ thống
top
htop
iostat -x 1
```

## Tài Liệu Tham Khảo

- [Tài Liệu Hadoop Chính Thức](https://hadoop.apache.org/docs/current/)
- [Hướng Dẫn Thiết Lập Cụm](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/ClusterSetup.html)
- [Kiến Trúc HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
- [Kiến Trúc YARN](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html)

## Hỗ Trợ

Đối với các vấn đề và câu hỏi:
- Kiểm tra [Hướng Dẫn Khắc Phục Sự Cố Hadoop](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/Troubleshooting.html)
- Ghé thăm [Danh Sách Gửi Thư Người Dùng Apache Hadoop](https://hadoop.apache.org/mailing_lists.html)
- Gửi vấn đề đến [Apache Hadoop JIRA](https://issues.apache.org/jira/projects/HADOOP)

---

**Lưu ý**: Hướng dẫn này dựa trên Hadoop 3.4.1. Cấu hình có thể thay đổi một chút cho các phiên bản khác nhau.