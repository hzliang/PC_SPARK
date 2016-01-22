项目说明：
  项目名称：基于MapReduce的海量数据主曲线算法学习研究
  使用软K段主曲线算法实现主曲线提取
  1、编写Spark代码，实现现有数据的港口到港口信息提取
  2、提出适合的方法，对港口到港口的数据进行过滤去噪
  3、使用Spark提供的MLlib机器学习算法库，对港口的信息进行聚类（聚类效果需要验证）
  4、使用Scala实现主曲线算法（需要验证算法的正确性）
  5、使用Scala实现改进蚁群算法（需要进行验证）
  6、使用Spark实现分布式主曲线算法（需要验证）



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
