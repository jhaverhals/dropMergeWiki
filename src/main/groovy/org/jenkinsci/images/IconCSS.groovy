package org.jenkinsci.images

class IconCSS {

    public static String getStyle() {
        StringBuilder sb = new StringBuilder('.jenkinsJobStatus{background-repeat: no-repeat; padding-left: 19px; width: 16px; height: 16px;}').append System.lineSeparator()

        Enumeration<URL> resources = IconCSS.class.getClassLoader().getResources(IconCSS.class.package.name.replace('.', '/'))
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            new File(resource.getFile()).listFiles().each { File f ->
                ['png', 'gif'].each { String extension ->
                    if (f.name.endsWith(".$extension")) {
                        sb.append '.jenkinsJobStatus_'
                        sb.append f.name[0..-5]
                        sb.append ' {background-image: url(data:image/' + extension + ';base64,'
                        sb.append f.bytes.encodeBase64().toString()
                        sb.append ')}'
                        sb.append System.lineSeparator()
                    }
                }
            }
        }

        return sb.toString()
    }
}
