基于MapReduce的海量数据主曲线算法学习研究

    1.分布式聚类
    2.局部主成分线段提取
    3.连接局部主成分线段

Ubuntu14.4（已测试）安装NativeSystemBLAS
sudo apt-get install libatlas3-base libopenblas-base
sudo update-alternatives --config libblas.so
sudo update-alternatives --config libblas.so.3
sudo update-alternatives --config liblapack.so
sudo update-alternatives --config liblapack.so.3

* 0            /usr/lib/openblas-base/libblas.so.3      40        自动模式
  1            /usr/lib/atlas-base/atlas/libblas.so.3   35        手动模式
  2            /usr/lib/openblas-base/libblas.so.3      40        手动模式

要维持当前值[*]请按回车键，或者键入选择的编号：1
update-alternatives: using /usr/lib/atlas-base/atlas/libblas.so.3 to provide /usr/lib/libblas.so.3 (libblas.so.3) in 手动模式

CentOS6.7
yum install blas blas-devel lapack lapack-devel atlas atlas-devel  --nogpgcheck

运行命令：
(standalone)
spark-submit --class run.Run --master spark://192.168.1.121:7077  
--executor-memory 6G  --total-executor-cores 40 /root/pc_spark.jar
/hzl/input/sp_5p.csv /hzl/input/clus
(Yarn)
spark-submit --class run.Run --master yarn-cluster --num-executors 4
--driver-memory 6g --executor-memory 6g --executor-cores 2 pc_spark.jar
/hzl/input/sin3.csv /hzl/output/clus

