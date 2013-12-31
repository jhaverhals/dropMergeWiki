package org.jenkinsci.images

import groovy.transform.Memoized

class IconCSS {

    @Memoized
    public static String getStyle() {
        StringBuilder sb = new StringBuilder('.jenkinsJobStatus{background-repeat: no-repeat; padding-left: 19px; width: 16px; height: 16px;}').append System.lineSeparator()

        def extraStyles = [grey: ['aborted', 'disabled'], grey_anime: ['aborted_anime', 'disabled_anime']]
        Enumeration<URL> resources = IconCSS.class.getClassLoader().getResources(IconCSS.class.package.name.replace('.', '/'))
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            new File(resource.getFile()).listFiles().each { File f ->
                ['png', 'gif'].each { String extension ->
                    if (f.name.endsWith(".$extension")) {
                        sb.append '.jenkinsJobStatus_'
                        sb.append f.name[0..-5]
                        sb.append ' '
                        if (extraStyles.containsKey(f.name[0..-5])) {
                            extraStyles[f.name[0..-5]].each { String extraStyle ->
                                sb.append ', .jenkinsJobStatus_'
                                sb.append extraStyle
                                sb.append ' '
                            }
                        }
                        sb.append '{background-image: url(data:image/' + extension + ';base64,'
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
