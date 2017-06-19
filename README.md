# WEKA Package for Regular Vines
This package provides a Regular Vine integration for the WEKA workbench.

It uses several copula functions ([see here](doc/Copula_Functions_for_Regular_Vines_Usage.pdf)) and a sequential selection method based on Kendall's tau and maximum likelihood estimation (Czado et al. 20??).

(DAAD REFERENCE)

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