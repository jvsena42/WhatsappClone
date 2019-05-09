package com.app.whatsapp.whatsappclone.activity;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.fragment.ContatosFragment;
import com.app.whatsapp.whatsappclone.fragment.ConversasFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Whatsapp");
        setSupportActionBar(toolbar); //Suporte a versoes anteriores

        //Configurar abas

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Conversas", ConversasFragment.class)
                .add("Contatos", ContatosFragment.class)
                .create()
        );

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuConfiguracoes:
                abrirConfiguracoes();
                break;
            case R.id.menuPesquisa:

                break;
            case R.id.menuSair:
                deslogarUsuario();
                finish();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){
        try {
            autenticacao.signOut();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirConfiguracoes(){
        Intent intent = new Intent(MainActivity.this,ConfiguracoesActivity.class);
        startActivity(intent);
    }
}
