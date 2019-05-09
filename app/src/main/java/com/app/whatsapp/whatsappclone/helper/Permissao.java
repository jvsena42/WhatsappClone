package com.app.whatsapp.whatsappclone.helper;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        if (Build.VERSION.SDK_INT >= 23){

            List<String> listaPermisses = new ArrayList<>();

           /*Percorre as permissoes passadas verificando uma a uma se
           ja tem a permissao liberada ou nao*/

           for (String permissao : permissoes){
               Boolean temPermissao = ContextCompat.checkSelfPermission(activity,permissao) == PackageManager.PERMISSION_GRANTED;
               if (!temPermissao) listaPermisses.add(permissao);
           }

           //Caso a lista esteja vazia
            if (listaPermisses.isEmpty()) return true;
           String[] novasPermissoes= new String[listaPermisses.size()];
           listaPermisses.toArray(novasPermissoes);

           //Solicitar permissao
            ActivityCompat.requestPermissions(activity,novasPermissoes,requestCode);
        }

        return true;
    }
}
