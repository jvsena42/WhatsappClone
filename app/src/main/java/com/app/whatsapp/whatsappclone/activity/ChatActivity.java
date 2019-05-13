package com.app.whatsapp.whatsappclone.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Configuracoes iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);

        //Recuperar dados do usuario destinatario
        Bundle bundle = getIntent().getExtras();

        if (bundle != null){
            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText(usuarioDestinatario.getNome());

            String foto = usuarioDestinatario.getFoto();
            if (foto != null){
                Uri url = Uri.parse(foto);
                Glide.with(ChatActivity.this)
                        .load(url)
                        .into(circleImageViewFoto);
            }else {
                circleImageViewFoto.setImageResource(R.drawable.padrao);
            }
        }
    }

}
