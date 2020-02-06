# UBUMonitor
[![GitHub release](https://img.shields.io/github/release/yjx0003/UBUMonitor.svg)](https://github.com/yjx0003/UBUMonitor/releases/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f79d51e496b3495690aa6480269536b8)](https://www.codacy.com/app/yjx0003/UBUMonitor?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yjx0003/UBUMonitor&amp;utm_campaign=Badge_Grade)
[![HitCount](http://hits.dwyl.io/yjx0003/UBUMonitor.svg)](http://hits.dwyl.io/yjx0003/UBUMonitor)
<p align="center"><img height="300" src="src/main/resources/img/logo.png" />

## Monitorización de alumnos en la plataforma Moodle
### Descripción:

Herramienta de visualización de calificaciones y registros (logs) del curso en diferentes tipos de gráficas.

Esta aplicación está dirigida a docentes que usen los servicios de Moodle.

Esta versión es la continuación de [UBUGrades](https://github.com/huco95/UBUGrades) de [Félix Nogal Santamaría](https://github.com/huco95).
<p align="center"><img src="/latex/img/ejemplo_barras_apiladas.png" />

## Manual de usuario:
<!--* https://ubumonitor.gitbook.io/ubumonitor/-->
* https://ubumonitordocs.readthedocs.io/es/latest/

## Dependencias:
La aplicación requiere **Java 8**.
Las librerías externas de **Java** están incluidas en la carpeta [lib](lib), de **JavaScript** en [resources/graphics/lib/](resources/graphics/lib/) y un pack de iconos.

### Dependencias de Java:
* [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/)
  * Versión: **1.6**
  * [Github](https://github.com/apache/commons-csv)
  * [Maven Repository](https://mvnrepository.com/artifact/org.apache.commons/commons-csv)
* [Apache Commons Math](https://commons.apache.org/proper/commons-math/)
  * Versión: **3.6.1**
  * [Github](https://github.com/apache/commons-math)
  * [Maven Repository](https://mvnrepository.com/artifact/org.apache.commons/commons-math3)
* [Apache Log4j](http://logging.apache.org/log4j/1.2/)
  * Versión: **1.2.17**
  * [Github](https://github.com/apache/log4j)
  * [Maven Repository](https://mvnrepository.com/artifact/log4j/log4j)
* [Gson](https://sites.google.com/site/gson/)
  * Version: **2.8.5**
  * [Github](https://github.com/google/gson)
  * [Maven Repository](https://mvnrepository.com/artifact/com.google.code.gson/gson)
* **JSON In Java**
  * Versión: **20180813**
  * [Github](https://github.com/stleary/JSON-java)
  * [Maven Repository](https://mvnrepository.com/artifact/org.json/json)
* [JSoup Java HTML Parser](https://jsoup.org/)
  * Versión: **1.11.3**
  * [Github](https://github.com/jhy/jsoup)
  * [Maven Repository](https://mvnrepository.com/artifact/org.jsoup/jsoup/1.11.3)
* [OpenCSV](http://opencsv.sourceforge.net/)
  * Versión: **4.6**
  * [GitHub](https://github.com/jlawrie/opencsv)
  * [Maven Repository](https://mvnrepository.com/artifact/com.opencsv/opencsv/4.6)
* [SLF4J API Module](https://www.slf4j.org/)
  * Versión: **1.7.26**
  * [Github](https://github.com/qos-ch/slf4j)
  * [Maven Repository](https://mvnrepository.com/artifact/org.slf4j/slf4j-api)
* [SLF4J LOG4J 12 Binding](https://www.slf4j.org/)
  * Versión: **1.7.26**
  * [Github](https://github.com/qos-ch/slf4j/tree/master/slf4j-log4j12)
  * [Maven Repository](https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12)
* [ThreeTen Extra](https://www.threeten.org/threeten-extra/)
  * Versión: **1.5.0**
  * [Github](https://github.com/ThreeTen/threeten-extra)
  * [Maven Repository](https://mvnrepository.com/artifact/org.threeten/threeten-extra)
  
### Dependencias de JavaScript:
* [ApexCharts](https://apexcharts.com/)
  * Versión: **3.10.1**
  * [Github](https://github.com/apexcharts/apexcharts.js)
  * [JSDELIVR Repository](https://www.jsdelivr.com/package/npm/apexcharts)
* [Chart.js](https://www.chartjs.org/)
  * Versión: **2.9.3**
  * [Github](https://github.com/chartjs/Chart.js)
  * [CDNJS Repository](https://cdnjs.com/libraries/Chart.js/)
* **Chart.js Box and Violin Plot**
  * Versión: **2.2.1**
  * [Github](https://github.com/datavisyn/chartjs-chart-box-and-violin-plot)
  * [UNPKG](https://unpkg.com/chartjs-chart-box-and-violin-plot@2/build/Chart.BoxPlot.js)
* **color-hash**
  * Versión: **1.0.3**
  * [Github](https://github.com/zenozeng/color-hash)
  * [jsDelivr Repository](https://www.jsdelivr.com/package/npm/color-hash)
* [Tabulator](http://tabulator.info/)
  * Versión: **4.5.2**
  * [Github](https://github.com/olifolkerd/tabulator)
  * [UNPKG](https://unpkg.com/tabulator-tables@4.5.2/dist/js/tabulator.min.js)
* [html2canvas](https://html2canvas.hertzen.com/)
  * Versión: **1.0.0-rc.5**
  * [Github](https://github.com/niklasvh/html2canvas/)
  
### Pack de iconos:
* [Font Awesome](https://fontawesome.com/)
  * Versión: **4.7.0**
  * [Github](https://github.com/FortAwesome/Font-Awesome/)
  * [fa2png](http://fa2png.io/r/font-awesome/)

## Autor

- Yi Peng Ji

## Tutores
- Raúl Marticorena Sánchez
- Carlos Pardo Aguilar

## Licencia
Este proyecto está licenciado bajo la licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.
