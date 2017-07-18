## DFG Project

This project is written in java.

---

#### Project structure 
* Dataset/

 This dir is used to store dataset. Files are stored in the format of `.dat`.
 * SPL.dat: SPL(FDA in other words) dataset.
 * Cancer.dat: Cancer(CAN in other words) dataset.
 * HIV.dat: HIV(AIDS in other words) dataset.
 * NCI.dat: NCI dataset.

* out/

 This dir is used to store the result of the algorithms we designed.

 * SPL/			
  The result of algorithms experiments on SPL dataset is stored 
  under this dir.

  * DFG/		

   This dir stores the result of DFG algorithm 
   experimenting on SPL dataset.  
   * Debug/

    This dir includes some debug info.
   * detail/

    This dir shows the frequent subgraphs represented 
    in  adjacency table.
   * threshold/

    This dir shows the mining result with its according threshold. 
   * top/

    This dir shows the mining result base on top-k.   

  * DiffFPM/

   This dir stores the result of DiffFPM algorithm 
   experimenting on SPL dataset.  

   * Debug/

    This dir includes some debug info.
   * detail/

    This dir shows the frequent subgraphs represented 
    in  adjacency table.
   * threshold/

    This dir shows the mining result with its according threshold. 
   * top/

    This dir shows the mining result base on top-k.
   * filter.txt

    Filter file. Used to filter some subgraphs. 
    By default, it's empty. 
    This means it doesn't filter any subgraphs.

  * Naive/

   This dir stores the result of Naive algorithm 
   experimenting on SPL dataset. 

   * Debug/

    This dir includes some debug info.
   * detail/

    This dir shows the frequent subgraphs represented 
    in  adjacency table.
   * threshold/

    This dir shows the mining result with its according threshold. 
   * top/

    This dir shows the mining result base on top-k.

  * FSG/

   This dir stores the result of FSG algorithm 
   experimenting on SPL dataset. 

   * Debug/    

    This dir includes some debug info.
   * detail/

    This dir shows the frequent subgraphs represented 
    in  adjacency table.

   * badsample/    

    This dir shows the mining result of FSG(fake) 
    algorithm experimenting on SPL dataset.

 * Cancer/

  The result of algorithms experiments on Cancer dataset is stored 
  under this dir. The structure of this dir is similar to the above.

 * HIV/

  The result of algorithms experiments on HIV dataset is stored 
  under this dir. The structure of this dir is similar to the above.

 * NCI/

  The result of algorithms experiments on NCI dataset is stored 
  under this dir. The structure of this dir is similar to the above.

* src/

 This dir stores source java codes. 
 * Apriori/
  * Apriori.java

   Implementation of the Apriori algorithm.
  * DFGFI1.java
  * NoiseCur.java
 * Common/

  Some common function in this project.
  * Contrast.java

   Implementation of functions to calculate Precision, Recall, F-Score and RE.
  * Distribution.java

   Implementation of the generation algorithm of 
   Laplace distribution and Geometric distribution.
  * Parameter.java

   Global parameters.
  * Permutation.java

   Get the permutation algorithm.
  * Sort.java

   Quick sort.
  * SortNode.java

   Data structure used in quick sort.

 * Dataset/

  This dir includes graph dataset processing functions.

  * Convertion.java

   Give every graph an id.
  * DatasetParameter.java

   Get dataset parameters.
  * Preprocessing.java

   Dataset preprocessing functions.
   * Sampling.java

   Sample on the dataset.
  * Statistic.java

   Get the summary info of the dataset.

 * DFG/
  * DFG.java

   Implementation of DFG algorithm.
  * Lattice.java

   Data structure used in ND2 phase.
  * Naive.java

   Implementation of Naive algorithm.

 * DiffFPM/

    Implementation of DFPM algorithm (2013 KDD).

    * DiffFPM.java

     Implementation of DFPM algorithm.
    * DiffFPMv1.java

     The first version of implementation of DFPM algorithm. 
     There is no optimization in this version.
    * LabelEdge.java

     The implementation of the edge data structure.

 * FSG/
  Implementation of FSG algorithm.

  * Automorphisms.java

   Implementation of subgraph automorphism.
  * Convertion.java

   Change canonical label into its according adjacency table.
  * FSG.java

   Implementation of FSG algorithm.
  * GraphSet.java

   Implementation of graph data structure used in FSG algorithm.
  * IsomorphismsTest.java

   Some test about isomorphism.
  * LabelGraphList.java

   Implementation of adjacency table.
  * LabelGraphMatrix.java

   Implementation of adjacency matrix.
  * LabelVertex.java

   Implementation of weighted vertex data structure.
  * LabelVertexList.java

   Implementation of weighted vertex list array structure.

 * FSG_fake/

  An old version implement of FSG algorithm, however, it is not 
  toughly correct(So it is called a `fake` version). 
  The frequent sub-graphs mined by this algorithm is used as the 
  random initialized candidate graph of DFPM algorithm.

  * Automorphisms.java

   Implementation of subgraph automorphism.
  * FSG.java

   Implementation of FSG(fake) algorithm.
  * GraphSet.java

   Implementation of graph data structure used in FSG algorithm.
  * LabelGraphList.java

   Implementation of adjacency table.
  * LabelGraphMatrix.java

   Implementation of adjacency matrix.
  * LabelVertex.java

   Implementation of weighted vertex data structure.
  * LabelVertexList.java

   Implementation of weighted vertex list array structure.
 * Jama/

  An open source class which is used for matrix computation.


---

## Steps to Start

#### Summary

1. Unzip Dataset.zip at the same dir and generate a dir named "Dataset".

2. Run FSG algorithm.
    Choose dataset as well as the threshold parameter, run FSG algorithm.

3. Choose an algorithm to run.
    * DFG algorithm: 

        Choose privacy budget，the num of edges of the graph with max num of edges. 
        Run DFG algorithm then you will get the F-score and RE. 

    * DFPM algorithm: 

        Choose privacy budget，threshold and its according top-k，
        as well as the random seed got from FSG algorithm. 
        Run DiffFPM.
    * Naive algorithm: 

        Choose privacy budget，
        the edge num of the graph with max edge num got from FSG algorithm.
        Run Naive algorithm.

#### Detail

1. Use FSG(or FSG_fake) to prepare parameters for the following algorithms.

 * Run FSG：run source file `/src/FSG/FSG.java`

  * Input parameters：
   * DataSet: Dataset name.
   * dbSize: The num of graphs in the dataset(See ==Appendix== part).
   * Threshold: Relative threshold(like 0.1)

  * Output：the result will be stored in `/out/[dataset name]/FSG/`

 * Run FSG_fake：run source file `/src/FSG_fake/FSG.java`

  * Input parameters：
   * DataSet: Dataset name.
   * dbSize: The num of graphs in the dataset(See ==Appendix== part).
   * Threshold: Relative threshold(like 0.1)

  * Output：The result of FSG(fake) will be stored in `/out/[dataset name]/FSG/badsample/`

2. Run algorithms using result produced by the programs above.

 1. Run DFG algorithm：run source file `/src/DFG/DFG.java`

  Be careful! ==DFG algorithm depends on the result file of FSG algorithm.==

  * Input parameters:
   * Dataset:Dataset name.
   * dbSize:The num of graphs in the dataset(See ==Appendix== part).
   * threshold:Relative threshold(like 0.1).
   * epsilon1:Privacy budget in $FS_1$ phase. 
   * epsilon2:Privacy budget in $ND_2$ phase. 
   * maxGraphSize: The estimated edge num of the graph with max edge num. 
     By default, it is generated using the result of FSG.
  * Input file：

   The result of FSG algorithm Using the same dataset and threshold. 
   The result file should be stored in this dir `out/[dataset name]/FSG/`
  * Output：

   The result of DFG will be stored in `out/[dataset name]/DFG/`

 2. Run DiffFPM algorithm：run source file `/src/DiffFPM/DiffFPM.java`

  Be careful! ==DiffFPM algorithm depends on the result file of FSG_fake algorithm.==

  * Input parameters:
   * Dataset: dataset name
   * dbSize: The num of graphs in the dataset(See ==Appendix== part).
   * k: The responding top-k of relative threshold.
   * threshold: Relative threshold.
   * epsilon1: Privacy budget in $FS_1$ phase. Often set to 0.1.
   * epsilon2: Privacy budget in $ND_2$ phase. Often set to 0.1.
  * Input files:

   * The result of FSG algorithm using the same dataset and threshold, 
     stored in `out/[dataset name]/FSG/`. 
   * The result of FSG_fake using the same dataset, 
     stored in `/out/[dataset name]/FSG/badsample/下`
   * `/out/[dataset name]/DiffFPM/filter.txt`, this file is often empty.
  * Output:

   The result of DFPM will be stored in `/out/[dataset name]/DiffFPM/`.

 3. Run Naive algorithm：run source file `/src/DFG/Naive.java`

  Be careful! ==Naive algorithm depends on the result file of FSG algorithm.==
  * Input parameters：
   * Dataset: Dataset name.
   * dbSize: The num of graphs in the dataset(See ==Appendix== part).
   * vertexType: num of vertex types in this dataset.
   * threshold: Relative threshold.
   * epsilon: Privacy budget.
   * maxGraphSize: The estimated edge num of the graph with max edge num. 
     By default, it is generated using the result of FSG.
  * Input file：

   The result of FSG algorithm Using the same dataset and threshold. 
   The result file should be stored in this dir `out/[dataset name]/FSG/`.
  * Output：

   The result of Naive algorithm will be stored in `out/[dataset name]/Naive/`


----


## Appendix：

1. The meta parameters of the dataset used in this paper.

 | Dataset name | Graph num | Num of vertex types |
 | :----------: | :-------: | :-----------------: |
 |     SPL      |   53084   |         104         |
 |    Cancer    |   32557   |         67          |
 |     HIV      |   42689   |         65          |
 |     NCI      |  265242   |         85          |

2. Parameters of SPL dataset

 | Relative threshold | Edge num of the estimated largest graph | responding top-k |
 | :----------------: | :-------------------------------------: | :--------------: |
 |        0.6         |                    9                    |        88        |
 |        0.65        |                    9                    |        68        |
 |        0.7         |                    8                    |        51        |
 |        0.75        |                    8                    |        38        |
 |        0.8         |                    6                    |        22        |

 | top-k | Edge num of the estimated largest graph | responding threshold |
 | :---: | :-------------------------------------: | :------------------: |
 |  20   |                    7                    |        0.811         |
 |  40   |                    7                    |        0.7369        |
 |  60   |                    9                    |        0.6765        |
 |  80   |                    9                    |        0.616         |
 |  100  |                    9                    |        0.582         |

3. Parameters of Cancer dataset

 | Relative threshold | Edge num of the estimated largest graph | responding top-k |
 | :----------------: | :-------------------------------------: | :--------------: |
 |        0.4         |                   10                    |       119        |
 |        0.45        |                   10                    |        90        |
 |        0.5         |                    9                    |        66        |
 |        0.55        |                    9                    |        51        |
 |        0.6         |                    8                    |        37        |

 | top-k | Edge num of the estimated largest graph | responding threshold |
 | :---: | :-------------------------------------: | :------------------: |
 |  20   |                    6                    |        0.7611        |
 |  40   |                    8                    |        0.5902        |
 |  60   |                    9                    |        0.5207        |
 |  80   |                    9                    |        0.4705        |
 |  100  |                    9                    |        0.4310        |


4.  Parameters of HIV dataset

 | Relative threshold | Edge num of the estimated largest graph | responding top-k |
 | :----------------: | :-------------------------------------: | :--------------: |
 |        0.4         |                   10                    |        98        |
 |        0.45        |                    9                    |        74        |
 |        0.5         |                    9                    |        56        |
 |        0.55        |                    8                    |        41        |
 |        0.6         |                    7                    |        32        |

 | top-k | Edge num of the estimated largest graph | responding threshold |
 | :---: | :-------------------------------------: | :------------------: |
 |  20   |                    6                    |        0.7231        |
 |  40   |                    8                    |        0.5542        |
 |  60   |                    9                    |        0.4894        |
 |  80   |                    9                    |        0.4386        |
 |  100  |                   10                    |        0.3971        |


5. Parameters of NCI dataset

 | Relative threshold | Edge num of the estimated largest graph | responding top-k |
 | :----------------: | :-------------------------------------: | :--------------: |
 |        0.65        |                    9                    |       109        |
 |        0.7         |                    8                    |        67        |
 |        0.75        |                    8                    |        55        |
 |        0.8         |                    7                    |        41        |
 |        0.85        |                    6                    |        24        |
