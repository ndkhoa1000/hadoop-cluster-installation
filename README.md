# Apache Hadoop Cluster Installation Guide

This comprehensive guide will walk you through installing and configuring an Apache Hadoop cluster, covering both single-node (pseudo-distributed) and multi-node (fully distributed) setups.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Download and Installation](#download-and-installation)
3. [Environment Configuration](#environment-configuration)
4. [Hadoop Configuration](#hadoop-configuration)
5. [Single-Node Setup (Pseudo-Distributed)](#single-node-setup)
6. [Multi-Node Setup (Fully Distributed)](#multi-node-setup)
7. [Starting the Cluster](#starting-the-cluster)
8. [Verification and Testing](#verification-and-testing)
9. [Common Issues and Troubleshooting](#troubleshooting)
10. [Useful Commands](#useful-commands)

## Prerequisites

### System Requirements
- **Operating System**: Linux, or Windows (with WSL)
- **Java**: OpenJDK 8, 11, or 17 (recommended: OpenJDK 11)
- **Memory**: Minimum 4GB RAM (8GB+ recommended for multi-node)
- **Disk Space**: Minimum 20GB available space
- **Network**: SSH access between nodes (for multi-node setup)

### Required Software
- Java Development Kit (JDK)
- SSH server and client
- rsync (for file synchronization)

## Download and Installation
### Step 0: Setup **master** user
``` bash
sudo adduser hadoop

# add password
sudo passwd hadoop

# add hadoop to sudo group
sudo adduser hadoop sudo 

# switch to hadoop user
su hadoop

#Navigate to hadoop home dir
cd ~
```

### Step 1: Download Hadoop

| Version | Release Date | Binary Download |
|---------|--------------|-----------------|
| 3.4.1   | 2024 Oct 18  | [hadoop-3.4.1.tar.gz](https://www.apache.org/dyn/closer.cgi/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz) |

```bash
# Download Hadoop 3.4.1
wget https://archive.apache.org/dist/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz

# Verify the download (optional)
wget https://downloads.apache.org/hadoop/common/hadoop-3.4.1/hadoop-3.4.1.tar.gz.sha512

shasum -a 512 -c hadoop-3.4.1.tar.gz.sha512
```

### Step 2: Extract and Install

```bash
# Extract the archive
tar -xzf hadoop-3.4.1.tar.gz

# Keep it in your preferred location
mv hadoop-3.4.1 ~/hadoop
```

## Environment Configuration

### Step 1: Install Java (if not already installed)

#### On Ubuntu/Debian:
```bash
sudo apt update && sudo apt install openjdk-11-jdk -y
```

#### On CentOS/RHEL:
```bash
sudo yum install java-11-openjdk-devel
```


### Step 2: Configure Environment Variables

Add the following to your `~/.bashrc` or `~/.profile`:

```bash
# Java Environment
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64  # Linux

# Hadoop Environment
export HADOOP_HOME=$HOME/hadoop  # or your installation path
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export HADOOP_MAPRED_HOME=$HADOOP_HOME
export HADOOP_COMMON_HOME=$HADOOP_HOME
export HADOOP_HDFS_HOME=$HADOOP_HOME
export YARN_HOME=$HADOOP_HOME

# Add Hadoop binaries to PATH
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```

Apply the changes:
```bash
source ~/.bashrc  # or ~/.profile
```

### Step 3: Configure SSH (Required for cluster operations)

```bash
# Generate SSH key pair (if not exists)
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa

# Add public key to authorized_keys
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

# Set appropriate permissions
chmod 0600 ~/.ssh/authorized_keys

# Test SSH to localhost
ssh localhost
```

## Hadoop Configuration

Navigate to the Hadoop configuration directory:
```bash
cd $HADOOP_HOME/etc/hadoop
```

### Step 1: Configure `hadoop-env.sh`

```bash
# Edit hadoop-env.sh
vim hadoop-env.sh

# Add or update the JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```

### Step 2: Configure Core Components

#### `core-site.xml`
```xml
<configuration>
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://localhost:9000</value>
        <description>The default file system URI</description>
    </property>
    <property>
        <name>hadoop.tmp.dir</name>
        <value>$HADOOP_HOME/tmp</value>
        <description>Temporary directory for Hadoop</description>
    </property>
</configuration>
```

#### `hdfs-site.xml`
```xml
<configuration>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>$HADOOP_HOME/data/namenode</value>
        <description>Directory for namenode metadata</description>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>$HADOOP_HOME/data/datanode</value>
        <description>Directory for datanode data</description>
    </property>
    <property>
        <name>dfs.replication</name>
        <value>1</value>
        <description>Default block replication</description>
    </property>
    <property>
        <name>dfs.namenode.checkpoint.dir</name>
        <value>$HADOOP_HOME/data/secondary</value>
        <description>Secondary namenode checkpoint directory</description>
    </property>
</configuration>
```

#### `mapred-site.xml`
```xml
<configuration>
    <property>
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
        <description>MapReduce framework name</description>
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
        <description>ResourceManager hostname</description>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
        <description>Auxiliary services for NodeManager</description>
    </property>
    <property>
        <name>yarn.nodemanager.env-whitelist</name>
        <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
    </property>
</configuration>
```

### Step 3: Create Required Directories

```bash
# Create data directories
sudo mkdir -p $HADOOP_HOME/data/{namenode,datanode,secondary}
sudo mkdir -p $HADOOP_HOME/tmp

# Set appropriate ownership
sudo chown -R $USER:$USER $HADOOP_HOME/data
sudo chown -R $USER:$USER $HADOOP_HOME/tmp
```

## Single-Node Setup

### Format the NameNode (First-time setup only)

```bash
hdfs namenode -format
```

### Start Hadoop Services

```bash
# Start HDFS
start-dfs.sh

# Start YARN
start-yarn.sh

# Or start all services at once
start-all.sh
```

### Verify the Installation

```bash
# Check running processes
jps

# Expected output should include:
# - NameNode
# - DataNode
# - ResourceManager
# - NodeManager
# - SecondaryNameNode
```

## Multi-Node Setup

### Prerequisites for Multi-Node

1. Multiple machines with Hadoop installed
2. Network connectivity between all nodes
3. SSH access from master to all slave nodes
4. Same username on all nodes
5. Synchronized time across all nodes

### Step 1: Configure Master Node

#### Update `core-site.xml` on master:
```xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://master:9000</value>
</property>
```

#### Update `yarn-site.xml` on master:
```xml
<property>
    <name>yarn.resourcemanager.hostname</name>
    <value>master</value>
</property>
```

#### Configure workers file:
```bash
# Edit $HADOOP_HOME/etc/hadoop/workers
vim $HADOOP_HOME/etc/hadoop/workers

# Add worker node hostnames (one per line)
# Remove localhost for cluster (if has)
node1
node2
node3
```
### Step 1.5: Configure Memory Allocation
Memory allocation can be tricky on low RAM nodes because default values are not suitable for nodes with less than 8GB of RAM. This section will highlight how memory allocation works for MapReduce jobs, and provide a sample configuration for **2GB RAM** nodes.

1. Update `yarn-site.xml`:
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
2. Update `mapred-site.xml`:
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
### Step 2: Configure Slave Nodes

1. Copy the entire Hadoop configuration from master to all slave nodes:
```bash
scp -r $HADOOP_HOME user@worker-node:~/
```

2. Update `hdfs-site.xml` on slaves to point to master:
```xml
<property>
    <name>dfs.namenode.name.dir</name>
    <value>$HADOOP_HOME/data/namenode</value>
</property>
```

### Step 3: Network Configuration

Update `/etc/hosts` on **all nodes**:
```bash
# Add entries for all nodes
192.168.1.100   master
192.168.1.101   node1
192.168.1.102   node2
192.168.1.103   node3
```

### Step 4: Start Multi-Node Cluster

From the master node:
```bash
# Format namenode (first time only)
hdfs namenode -format

# Start the cluster
start-all.sh
```

## Starting and Stopping Hadoop Cluster

### 🎯 **Quick Start (Recommended, using written script in this Github repository)**

Use the convenient control script included in this repository:

```bash
# Start the cluster
./hadoop-control.sh start

# Stop the cluster  
./hadoop-control.sh stop

# Restart the cluster
./hadoop-control.sh restart

# Check cluster status
./hadoop-control.sh status

# Show help
./hadoop-control.sh help
```

This script provides:
- ✅ **Colored output** for easy reading
- ✅ **Automatic error checking** and validation
- ✅ **Smart service detection** - knows what's running
- ✅ **Web interface links** when cluster is healthy
- ✅ **Safe start/stop procedures** with proper sequencing

### 🚀 Manual Service Management

#### Method 1: Start All Services at Once

```bash
# Start all Hadoop services
start-all.sh

# Check if all services are running
jps
```

#### Method 2: Start Services Individually

```bash
# 1. Start HDFS services (NameNode, DataNode, SecondaryNameNode)
start-dfs.sh

# 2. Start YARN services (ResourceManager, NodeManager)
start-yarn.sh

# 3. Start MapReduce Job History Server (optional)
mapred --daemon start historyserver
```

#### Method 3: Start Services One by One

```bash
# Start NameNode
hdfs --daemon start namenode

# Start DataNode
hdfs --daemon start datanode

# Start SecondaryNameNode
hdfs --daemon start secondarynamenode

# Start ResourceManager
yarn --daemon start resourcemanager

# Start NodeManager
yarn --daemon start nodemanager

# Start Job History Server
mapred --daemon start historyserver
```

### ⏹️ Stopping Hadoop Services

#### Stop All Services

```bash
# Stop all Hadoop services
stop-all.sh
```

#### Stop Services Individually

```bash
# Stop YARN services
stop-yarn.sh

# Stop HDFS services
stop-dfs.sh

# Stop Job History Server
mapred --daemon stop historyserver
```

#### Stop Services One by One

```bash
# Stop Job History Server
mapred --daemon stop historyserver

# Stop NodeManager
yarn --daemon stop nodemanager

# Stop ResourceManager
yarn --daemon stop resourcemanager

# Stop SecondaryNameNode
hdfs --daemon stop secondarynamenode

# Stop DataNode
hdfs --daemon stop datanode

# Stop NameNode
hdfs --daemon stop namenode
```

### 🔄 Restart Services

```bash
# Restart all services
stop-all.sh && start-all.sh

# Restart HDFS only
stop-dfs.sh && start-dfs.sh

# Restart YARN only
stop-yarn.sh && start-yarn.sh
```

### ✅ Verify Services are Running

#### Check Running Java Processes

```bash
# List all Hadoop-related Java processes
jps

# Expected output should include:
# 12345 NameNode
# 12346 DataNode
# 12347 SecondaryNameNode
# 12348 ResourceManager
# 12349 NodeManager
# 12350 JobHistoryServer (if started)
```

#### Check Specific Service Status

```bash
# Check if NameNode is running
hdfs dfsadmin -report

# Check if YARN is running
yarn node -list

# Check cluster health
hdfs dfsadmin -safemode get
```

### 🧪 Testing Hadoop Installation

#### Test 1: Basic HDFS Operations

```bash
# Create your user directory in HDFS
hdfs dfs -mkdir -p /user/$USER

# Create a test directory
hdfs dfs -mkdir /user/$USER/test

# List HDFS root directory
hdfs dfs -ls /

# List your user directory
hdfs dfs -ls /user/$USER

# Check HDFS health
hdfs fsck /
```

#### Test 2: File Upload and Download

```bash
# Create a test file locally
echo "Hello Hadoop World!" > test.txt
echo "This is a test file for Hadoop HDFS" >> test.txt

# Upload file to HDFS
hdfs dfs -put test.txt /user/$USER/

# List files in HDFS
hdfs dfs -ls /user/$USER/

# View file content in HDFS
hdfs dfs -cat /user/$USER/test.txt

# Download file from HDFS
hdfs dfs -get /user/$USER/test.txt downloaded_test.txt

# Verify the downloaded file
cat downloaded_test.txt

# Clean up test files
rm test.txt downloaded_test.txt
hdfs dfs -rm /user/$USER/test.txt
```

#### Test 3: Run Sample MapReduce Job

```bash
# Create input directory for MapReduce
hdfs dfs -mkdir /input

# Copy Hadoop configuration files as input
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml /input/

# Run the word count example
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar wordcount /input /output

# Check the output
hdfs dfs -ls /output/
hdfs dfs -cat /output/part-r-00000 | head -20

# Clean up
hdfs dfs -rm -r /output
hdfs dfs -rm -r /input
```

#### Test 4: YARN Application Test

```bash
# Run a simple YARN application (Pi calculation)
yarn jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-*.jar pi 2 100

# Check application history
yarn application -list -appStates ALL
```

#### Test 5: Performance Benchmark Tests

```bash
# TestDFSIO Write Test
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -write -nrFiles 4 -fileSize 128MB

# TestDFSIO Read Test
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -read -nrFiles 4 -fileSize 128MB

# Clean up test data
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-jobclient-*-tests.jar TestDFSIO -clean
```

### 📊 Monitoring and Health Checks

#### Web Interface Access

```bash
# Open web interfaces (run these commands to get URLs)
echo "NameNode Web UI: http://localhost:9870"
echo "ResourceManager Web UI: http://localhost:8088"
echo "Job History Server: http://localhost:19888"
echo "DataNode Web UI: http://localhost:9864"
echo "NodeManager Web UI: http://localhost:8042"
```

#### Command Line Monitoring

```bash
# Check cluster summary
hdfs dfsadmin -report

# Check filesystem
hdfs fsck /

# Monitor YARN applications
yarn top

# Check node status
yarn node -list -showDetails

# View cluster metrics
yarn cluster -lnl
```

## 🔧 Cluster Node Management

### Node Management Script

I've created a comprehensive script to manage cluster nodes:

```bash
# Make the script executable (if not already)
chmod +x ./manage-cluster-nodes.sh

# Show all available commands
./manage-cluster-nodes.sh help
```

### Key Features

#### 📋 **Node Information**
```bash
# List all current nodes
./manage-cluster-nodes.sh list

# Show detailed cluster status
./manage-cluster-nodes.sh status
```

#### ➕ **Adding Nodes**
```bash
# Add a new worker node
./manage-cluster-nodes.sh add node1

# Add node by IP address
./manage-cluster-nodes.sh add 192.168.1.100
```

#### ➖ **Removing Nodes (Safe Process)**
```bash
# Step 1: Safely decommission the node
./manage-cluster-nodes.sh decommission node1

# Step 2: Remove from cluster (after decommissioning completes)
./manage-cluster-nodes.sh remove node1
```

#### 🔄 **Node Management**
```bash
# Bring back a decommissioned node
./manage-cluster-nodes.sh recommission node1
```

#### 🏗️ **Cluster Conversion**
```bash
# Convert single-node to multi-node setup
./manage-cluster-nodes.sh convert-multi

# Convert multi-node back to single-node
./manage-cluster-nodes.sh convert-single
```

#### 💾 **Configuration Backup & Restore**
```bash
# Backup current configuration
./manage-cluster-nodes.sh backup

# Restore from backup
./manage-cluster-nodes.sh restore
```

### Adding a New Worker Node - Complete Process

1. **Add node to cluster configuration:**
   ```bash
   ./manage-cluster-nodes.sh add node2
   ```

2. **Set up SSH passwordless access:**
   ```bash
   # Copy SSH key to new node
   ssh-copy-id user@node2
   
   # Test SSH access
   ssh node2
   ```

3. **Install Hadoop on the new node:**
   ```bash
   # Copy Hadoop installation to new node
   scp -r $HADOOP_HOME user@node2:~/
   ```

4. **Copy configuration files:**
   ```bash
   # Copy configuration to new node
   scp -r $HADOOP_HOME/etc/hadoop/* user@node2:$HADOOP_HOME/etc/hadoop/
   ```

5. **Refresh cluster nodes:**
   ```bash
   # Refresh YARN nodes
   yarn rmadmin -refreshNodes
   
   # Refresh HDFS nodes
   hdfs dfsadmin -refreshNodes
   ```

6. **Start services on new node:**
   ```bash
   # On the new worker node, start DataNode and NodeManager
   ssh node2 "$HADOOP_HOME/bin/hdfs --daemon start datanode"
   ssh node2 "$HADOOP_HOME/bin/yarn --daemon start nodemanager"
   ```

### Safely Removing a Node - Complete Process

1. **Decommission the node:**
   ```bash
   ./manage-cluster-nodes.sh decommission node2
   ```

2. **Monitor decommissioning progress:**
   ```bash
   # Check HDFS decommissioning status
   hdfs dfsadmin -report
   
   # Check YARN node status
   yarn node -list -all
   ```

3. **Wait for decommissioning to complete** (data blocks are moved to other nodes)

4. **Remove the node:**
   ```bash
   ./manage-cluster-nodes.sh remove node2
   ```

5. **Stop services on the removed node:**
   ```bash
   ssh node2 "$HADOOP_HOME/bin/yarn --daemon stop nodemanager"
   ssh node2 "$HADOOP_HOME/bin/hdfs --daemon stop datanode"
   ```

### Configuration Files Modified

The script automatically manages these files:
- `$HADOOP_HOME/etc/hadoop/workers` - List of worker nodes
- `$HADOOP_HOME/etc/hadoop/core-site.xml` - Core configuration
- `$HADOOP_HOME/etc/hadoop/yarn-site.xml` - YARN configuration  
- `$HADOOP_HOME/etc/hadoop/dfs.exclude` - HDFS decommission list
- `$HADOOP_HOME/etc/hadoop/yarn.exclude` - YARN decommission list

### Backup and Recovery

All configuration changes are automatically backed up to:
```
$HADOOP_HOME/backups/hadoop_config_YYYYMMDD_HHMMSS.tar.gz
```

You can restore any backup using:
```bash
./manage-cluster-nodes.sh restore
```

### Quick Reference

For a complete quick reference guide, see: `CLUSTER-MANAGEMENT.md`

---
## Verification and Testing

### Check Cluster Status

```bash
# Check HDFS status
hdfs dfsadmin -report

# Check YARN nodes
yarn node -list

# Check running processes
jps
```

### Web Interfaces

- **HDFS NameNode**: http://localhost:9870
- **YARN ResourceManager**: http://localhost:8088
- **MapReduce Job History**: http://localhost:19888

### Basic HDFS Operations

```bash
# Create directories in HDFS
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/$USER

# List HDFS contents
hdfs dfs -ls /

# Copy file to HDFS
hdfs dfs -put /path/to/local/file /user/$USER/

# Copy file from HDFS
hdfs dfs -get /user/$USER/file /path/to/local/
```

### Run Sample MapReduce Job

```bash
# Create input directory
hdfs dfs -mkdir /input

# Copy input files
hdfs dfs -put $HADOOP_HOME/etc/hadoop/*.xml /input

# Run word count example
hadoop jar $HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.4.1.jar wordcount /input /output

# Check output
hdfs dfs -cat /output/part-r-00000
```

## Troubleshooting

### Common Issues and Solutions

#### 0. Quick file sync using **rsync**:
Using ***scp*** might not sync file across all nodes. So ***rsync*** will map and replace all files with the same name and make sure they will be updated exactly.
```bash
rsync -avz ~/hadoop/etc/hadoop/ user@node1:~/hadoop/etc/hadoop/

```

#### 1. Java-related Errors
```bash
# Verify Java installation
java -version
echo $JAVA_HOME

# Ensure JAVA_HOME is set in hadoop-env.sh
```

#### 2. SSH Connection Issues
```bash
# Test SSH connectivity
ssh localhost
ssh node1

# Check SSH key setup
ls -la ~/.ssh/
```

#### 3. Permission Denied Errors
```bash
# Fix directory permissions
sudo chown -R $USER:$USER $HADOOP_HOME
chmod 755 $HADOOP_HOME/data/*
```

#### 4. Port Already in Use
```bash
# Check what's using the port
netstat -tulpn | grep :9000
lsof -i :9000

# Kill the process if necessary
kill -9 <PID>
```

#### 5. DataNode Not Starting
```bash
# Check logs
tail -f $HADOOP_HOME/logs/hadoop-*-datanode-*.log

# Common solution: Remove and reformat
stop-all.sh
rm -rf $HADOOP_HOME/data/datanode/*
hdfs namenode -format -force
start-all.sh
```

### Log Files Location

```bash
# Hadoop logs directory
$HADOOP_HOME/logs/

# Important log files
hadoop-*-namenode-*.log
hadoop-*-datanode-*.log
yarn-*-resourcemanager-*.log
yarn-*-nodemanager-*.log
```

## Useful Commands

### HDFS Commands
```bash
# File system check
hdfs fsck /

# Safe mode operations
hdfs dfsadmin -safemode leave
hdfs dfsadmin -safemode enter

# Balance cluster
hdfs balancer

# Decommission nodes
hdfs dfsadmin -refreshNodes
```

### YARN Commands
```bash
# List applications
yarn application -list

# Kill application
yarn application -kill <application_id>

# Node management
yarn node -list -all
yarn rmadmin -refreshNodes
```

### Cluster Administration
```bash
# Check cluster health
hdfs dfsadmin -report
yarn node -list -showDetails

# Monitor cluster
hadoop dfsadmin -printTopology
yarn top
```

### Performance Monitoring
```bash
# Check disk usage
hdfs dfs -du -h /

# Monitor system resources
top
htop
iostat -x 1
```

## References

- [Official Hadoop Documentation](https://hadoop.apache.org/docs/current/)
- [Cluster Setup Guide](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/ClusterSetup.html)
- [HDFS Architecture](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
- [YARN Architecture](https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html)

## Support

For issues and questions:
- Check the [Hadoop Troubleshooting Guide](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/Troubleshooting.html)
- Visit [Apache Hadoop User Mailing List](https://hadoop.apache.org/mailing_lists.html)
- Submit issues to [Apache Hadoop JIRA](https://issues.apache.org/jira/projects/HADOOP)

---

**Note**: This guide is based on Hadoop 3.4.1. Configuration may vary slightly for different versions.
