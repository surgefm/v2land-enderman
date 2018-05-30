package enderman

object Page extends Config {

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
      |  <script src="${scriptSrc}"></script>
      |  <script>
      |    Main.main()
      |  </script>
      |</body>
      |</html>
     """.stripMargin
  }

  def scriptSrc: String = {
    if (env == "production") {
      "/public/client-opt.js"
    } else {
      "/public/client-fastopt.js"
    }
  }

}
