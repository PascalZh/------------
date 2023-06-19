所有java程序在根目录运行，matlab程序在文件所在目录运行。

# 程序说明
## 1阶模型程序说明
只需提供 W.csv 和 D.csv 这两个文件即可

fixNode 表示固定加氯点，可以自行修改哪个点是固定加氯点，具体只需找到这个变量相关的所有代码，自己修改

numNodes 节点总数默认为36个

## 2阶模型程序说明
### gen_second_order_model.m
seg_node_table 即论文的表 4-1 前 3 列

t Q k 以管段号为索引，也就是和 seg_node_table 的第一列是对应的

手动修改optim_vars成1阶模型所得的加氯点，再运行程序即可生成 A.csv

### SecondOrderModel.java
上一步生成 A.csv 后，直接运行即可，如果需要修改约束，可自行在程序里修改，这个很简单
