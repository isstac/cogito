# Cogito
> A brain for symbolic execution.

Cogito is a tool for exploring the most costly paths of programs with input configurations that would otherwise be intractable for exhaustive analysis. It can therefore be used to derive complexity bounds.

Cogito applies a two-phase approach. In phase 1 it conducts exhaustive exploration for a range of input sizes and collects all the paths with maximum cost. These paths become *training data* for a classification algorithm (currently logistic regression), that gets associated with each conditional statement in the program. The classifiers are thus trained to predict which choice to make at the condition given the current path (i.e. sequence of prior decisions) in order to explore the path with maximum cost. 

In phase 2, symbolic execution is again performed, but whenever a condition with symbolic predicate is encountered, the classification algorithm is used for deciding which choice to make. This enables symbolic execution to scale far beyond the capabilities of exhaustive analysis, since many choices are pruned. 

Cogito records the maximum cost found for input size 1,..,N (where N is provided by the user) during the guided search. This data is used with OLS regression to fit functions corresponding to the common complexity classes. The functions can be visualized and analyzed for deriving complexity bounds, finding performance issues, etc.

## Installation
Make sure that `jpf-core` and `jpf-symbc` are properly installed.

To install Cogito, update your `site.properties` file (usually `~/.jpf/site.properties`) and set the `canopy` variable to point to the directory of your Cogito installation. 
```
cogito=/path/to/cogito
```

Do **not** add `cogito` to the `extensions` variable.

Make sure you have `Ivy` installed on your system. To bootstrap the Ivy ant task, you can run:
```
$ ant bootstrap
```

Then, obtain all the dependencies by running:
```
$ ant resolve
```
The dependencies will be downloaded to `lib/`.

Now Cogito can be built by simply running:
```
$ ant build
```

## Usage

The analysis can be performed by executing the JPF config file that specifies the parameters of the analysis, the constraint solver, the entry point of the system under analysis etc:

```
$ ./jpf-core/bin/jpf <path-to-jpf-file>
```

## Configuration
Cogito has global and analysis local configuration options. Configuration of Cogito happen through the jpf file.

To enable Cogito, the JPF file **must** contain the `@using` directive:
```
@using cogito
```

Cogito relies on the configuration options available in Java PathFinder and Symbolic PathFinder and can use the incremental solver in Symbolic PathFinder. Please consult these projects regarding configuration.

Most importantly is that the JPF file contains values for `target` (and possibly `classpath` if the SUT is not part of Cogito), `symbolic.method`, and `symbolic.dp`.


The options for Cogito are:

* **cogito.training.target.args=low,high** The input sizes from `low` to `high` where data will be gathered (i.e. paths with max cost) for training the classifiers. To train for a specific input size, set `low` and `high` to the same value.
* **cogito.prediction.target.args=size** The maximum input size at which guided search will be performed. Uses guided search for 1,2,..,size (inclusive).
* **cogito.visualize=boolean** If `true`, a window will appear showing the raw data obtained from the guided analysis as well as fitted functions corresponding to common complexity classes.
* **cogito.costmodel.measuredmethod=method name** Specifies the method at which cost will start accumulating. This is necessary when analyzing SUTs where, e.g., a data structure is being built before applying some operation on it (such as searching in a binary tree with symbolic elements).
* **cogito.costmodel=class name** The fully qualified class name for an implementation of `edu.cmu.sv.isstac.cogito.cost.CostModel` that specifies which cost model to use. Defaults to `DepthCostModel` if left unspecified.
* **cogito.prediction.confidence=double** a value ]0.0;1.0] specifying the threshold for the posterior probabilities of the predictions made by the classifiers. If the posterior probabilities are not greater than this number, exploration degenerates to an exhaustive analysis at that decision (i.e. all choices will be explored). By default there is no threshold, i.e. the prediction with posterior probability >0.5 will be selected (assuming binary choices).
* **cogito.output** Will output statistics about the training and guided search runs to CSV files. The traning (guided search) statistics will be located on the specified path in a file with name `target_training.csv` (`target_guidance.csv`) where `target` is the SUT name. 


## LICENSE

Cogito is Copyright (c) 2017, Carnegie Mellon University and is released under the MIT License. See the `LICENSE` file in the root of this project and the headers of the individual files in the `src/` folder for the details.


