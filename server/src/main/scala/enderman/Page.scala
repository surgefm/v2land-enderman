package enderman

object Page {

  def index: String = {
    s"""<!DOCTYPE html>
      |<html lang="zh-hans">
      |<head>
      |  <meta charset="UTF-8">
      |  <meta name="viewport" content="width=device-width, initial-scale=1.0">
      |  <meta http-equiv="X-UA-Compatible" content="ie=edge">
      |  <title>Enderman Data Center</title>
      |  <link rel="stylesheet" type="text/css" href="public/css/bulma.min.css">
      |</head>
      |<body>
      |  <div id="app"></div>
      |  <canvas id="chart-recent7days"></canvas>
      |  <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.2/Chart.min.js"></script>
      |</body>
      |</html>
     """.stripMargin
  }

}
