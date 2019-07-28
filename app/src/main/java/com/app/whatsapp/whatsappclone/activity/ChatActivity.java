package com.app.whatsapp.whatsappclone.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.adapter.MensagensAdapter;
import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.app.whatsapp.whatsappclone.helper.Base64Custom;
import com.app.whatsapp.whatsappclone.helper.UsuarioFirebase;
import com.app.whatsapp.whatsappclone.model.Conversa;
import com.app.whatsapp.whatsappclone.model.Grupo;
import com.app.whatsapp.whatsappclone.model.Mensagem;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private EditText editMensagem;
    private ImageView imageCamera;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private StorageReference storage;
    private ChildEventListener childEventListenerMensagens;
    private Grupo grupo;

    private static final int SELECAO_CAMERA = 100;

    private RecyclerView recyclerMensagens;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();


    //Identificador usuario remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;



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
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);

        //Recuperar dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //Recuperar dados do usuario destinatario
        Bundle bundle = getIntent().getExtras();

        if (bundle != null){

            if(bundle.containsKey("chatGrupo")){
                /*Conversa de grupo*/
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                textViewNome.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                String foto = grupo.getFoto();
                if (foto != null){
                    Uri url = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(url)
                            .into(circleImageViewFoto);
                }else {
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else {
                /*Conversa Convencional*/
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

                //recuperar id do usuario destinatario
                //idUsuarioDestinatario = usuarioDestinatario.getId();
                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }

        }

        //Configurar adapter
        adapter = new MensagensAdapter(mensagens,getApplicationContext());

        //Configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager(layoutManager);
        recyclerMensagens.setHasFixedSize(true);
        recyclerMensagens.setAdapter(adapter);

        //Recuperar instancia do firebase database
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        //Recuperar instancia do firebase storage
        storage = ConfiguracaoFirebase.getFirebaseStorage();

        //Evento de clique da câmera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent,SELECAO_CAMERA);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            Bitmap imagem = null;

            try {

                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }

                if (imagem != null){

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos );
                    byte[] dadosImagem = baos.toByteArray();

                    //Criar nome da imagem;
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar referencias do firebase
                    final StorageReference imagemRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(dadosImagem);

                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continua a tarefa para pegar a URL.
                            return imagemRef.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();
                                String downloadUrl = downloadUri.toString();

                                Mensagem mensagem = new Mensagem();
                                mensagem.setIdUsuario(idUsuarioRemetente);
                                mensagem.setMensagem("imagem.jpg");
                                mensagem.setImagem(downloadUrl);

                                // Salvar mensagem remetente
                                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                // Salvar mensagem destinatario
                                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                                Toast.makeText(ChatActivity.this, "Foto enviada com sucesso!", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(ChatActivity.this, "Erro em enviar a foto: " + task.getException(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void enviarMensagem(View view){
        String textoMensagem = editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()){

            if (usuarioDestinatario != null){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //Salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);

                //Salvar mensagem para o destinatário
                salvarMensagem(idUsuarioDestinatario,idUsuarioRemetente,mensagem);

                //Salvar conversa
                salvarConversa(mensagem,false);

            }else {

                for (Usuario membro: grupo.getMembros()){

                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);

                    //Salvar mensagem para o membro
                    salvarMensagem(idUsuarioRemetente,idUsuarioDestinatario,mensagem);

                    //Salvar Conversa
                    salvarConversa(mensagem,true);
                }

            }


        }else {
            Toast.makeText(ChatActivity.this,"Digite uma mensagem",Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarConversa (Mensagem msg, Boolean isGroup){

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idUsuarioRemetente);
        conversaRemetente.setIdDDestinatario(idUsuarioDestinatario);
        conversaRemetente.setUltimaMensagem(msg.getMensagem());

        if (isGroup){
            //Conversa de grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);

        }else {
            //Conversa convencional
            conversaRemetente.setUsuarioExibicao(usuarioDestinatario);
            conversaRemetente.setIsGroup("false");
        }

        conversaRemetente.salvar();

    }

    private void salvarMensagem(String idRemetente,String idDestinatario, Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //Limpar texto
        editMensagem.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
        mensagens.clear();

    }

    private void recuperarMensagens(){

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}
