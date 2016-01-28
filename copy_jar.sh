#!/usr/bin/env bash
zip -d /home/hy/PC_SPARK/out/artifacts/pc_spark_jar/pc_spark.jar META-INF/*.RSA META-INF/*.DSA META-INF/*.SF
scp /home/hy/PC_SPARK/out/artifacts/pc_spark_jar/pc_spark.jar root@192.168.1.121:./
#ssh root@192.168.1.121 'cat pc.log'
