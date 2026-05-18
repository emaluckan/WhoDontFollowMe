package org.example.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.stream.Stream;
import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

@Service
public class InstagramService {

    public List<String> procesarZip(MultipartFile file) throws IOException {

        // carpeta temporal
        Path tempDir = Files.createTempDirectory("instagram_");

        // guardar zip
        Path zipPath = tempDir.resolve(file.getOriginalFilename());

        Files.copy(file.getInputStream(), zipPath);

        // descomprimir
        unzip(zipPath.toString(), tempDir.toString());

        System.out.println("ZIP descomprimido en: " + tempDir);

        Path followingPath = buscarArchivo(tempDir, "following.html");
        Path followersPath = buscarArchivo(tempDir, "followers_1.html");

        System.out.println("FOLLOWING: " + followingPath);
        System.out.println("FOLLOWERS: " + followersPath);

        List<String> siguiendo = leerUsuarios(followingPath);
        List<String> seguidores = leerUsuarios(followersPath);

        List<String> noSiguen = new ArrayList<>();

        for (String usuario : siguiendo) {

            if (!seguidores.contains(usuario)) {
                noSiguen.add(usuario);
            }
        }

        Collections.sort(noSiguen);

        return noSiguen;

    }

    private void unzip(String zipFilePath, String destDirectory) throws IOException {

        File destDir = new File(destDirectory);

        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath));

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {

            File newFile = new File(destDir, zipEntry.getName());

            if (zipEntry.isDirectory()) {

                newFile.mkdirs();

            } else {

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
            }

            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    private Path buscarArchivo(Path root, String nombreArchivo) throws IOException {

        try (Stream<Path> paths = Files.walk(root)) {

            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(nombreArchivo))
                    .findFirst()
                    .orElse(null);
        }
    }

    private List<String> leerUsuarios(Path archivoHtml) throws IOException {

        List<String> usuarios = new ArrayList<>();

        Document doc = Jsoup.parse(archivoHtml.toFile(), "UTF-8");

        Elements links = doc.select("a[href*=\"instagram.com/\"]");

        for (Element link : links) {

            String url = link.attr("href").trim();

            String usuario = extraerUsername(url);

            if (
                    usuario != null &&
                            !usuario.isEmpty() &&
                            !usuario.startsWith("__deleted__")
            ) {

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }

    private String extraerUsername(String url) {

        try {

            if (url.contains("?")) {
                url = url.substring(0, url.indexOf("?"));
            }

            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            if (url.contains("/_u/")) {
                return url.substring(url.indexOf("/_u/") + 4);
            }

            return url.substring(url.lastIndexOf("/") + 1);

        } catch (Exception e) {

            return null;
        }
    }
}