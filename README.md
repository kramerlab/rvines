# WEKA Package for Regular Vines
This package provides a Regular Vine integration for the WEKA workbench.

It uses several copula functions ([see here](doc/Copula_Functions_for_Regular_Vines_Usage.pdf)) and a sequential selection method based on Kendall's tau and maximum likelihood estimation.

This work was supported by a fellowship within the FITweltweit
programme of the German Academic Exchange Service (DAAD).

## How-To Build RVine Weka Package
To (re-)build the package an installation of Apache Ant is required.

Navigate Console into the vines package and use the following ant command to build the package:

```
ant -f build_package.xml -Dpackage=vines-1.0.0 make_package
```

The **_.zip_** is placed inside the **_dist_** folder.

## How-To Install RVine Weka Package

Use the [WEKA Package Manager](https://weka.wikispaces.com/How+do+I+use+the+package+manager%3F)

## How-To Run RVine Weka Package

You can run the package via console using:

```
java -cp (path)/weka.jar weka.Run RegularVine
```

Or use the RVine Panel inside the WEKA Explorer.

(**_Note:_** Make sure your data is normalized)

## References

Dissmann, J., Brechmann, E. C., Czado, C., & Kurowicka, D. Selecting and estimating regular vine copulae and application to financial returns. Computational Statistics & Data Analysis, 59:52-69,2013.

Song, P. X.-K. Multivariate dispersion models generated from gaussian copula.
Scandinavian Journal of Statistics, 27(2):305–320, 2000.

Aas, K., Czado, C., Frigessi, A. and Bakken, H. Pair-copula constructions
of multiple dependence. Insurance Mathematics and Economics, 44(2):182–198,
2009.

Fang, H. B., Fang, K. T., and Kotz, S. The meta-elliptical distributions with
given marginals. Journal of Multivariate Analysis, 82(1):1–16, 2002.

Genest, C., and MacKay, J. The joy of copulas: bivariate distributions with
uniform marginals. The American Statistician, 40(4):280–283, 1986.

Genest, C., and Favre, A. C. Everything you always wanted to know about
copula modeling but were afraid to ask. Journal of hydrologic engineering,
12(4):347–368, 2007.

Mahfoud, M., and Michael, M. Bivariate archimedean copulas: an application
to two stock market indices. BMI Paper, 2012.

Genest, C., Masiello, E., Tribouley, K. Estimating copula densities through
wavelets. Insurance: Mathematics and Economics, 44(2):170–181, 2009.

Genest, C., Kojadinovic, I., NeÅa˛lehovÃa˛, J., and Yan, J. A goodness-of-fit test
for bivariate extreme-value copulas. Bernoulli, 17(1):253–275, 2011.

HU Berlin. Multivariate Time Series. http://fedc.wiwi.hu-berlin.de/
xplore/tutorials/stfhtmlnode13.html. [Online; accessed 05-March-2017].

Schirmacher, D., and Schirmacher, E. Multivariate dependence modeling
using pair-copulas. Technical report, pages 14–16, 2008.

Doyon, G. On densities of extreme value copulas. M.Sc. Thesis, ETH Zurich,
2013.
