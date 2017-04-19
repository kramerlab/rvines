# rvines
Build RVine Weka Package
Navigate Console into the vines package.
use the following ant command to build the package:

ant -f build_package.xml -Dpackage=vines-1.0.0 make_package

Use the weka package manager to list installed packages:
(fill in the pathways to the file)
java -cp path/weka.jar weka.core.WekaPackageManager -list-packages installed

Use the weka package manager to install the package:
(fill in the pathways to the files)
java -cp path/weka.jar weka.core.WekaPackageManager -install-package path/vines-1.0.0.zip

Use the weka package manager to uninstall the package:
(fill in the pathways to the file)
java -cp path/weka.jar weka.core.WekaPackageManager -uninstall-package vines

After installing, you can run the RegularVines using Weka:
(fill in the pathways to the file)
java -cp path/weka.jar weka.Run RegularVine