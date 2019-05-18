package com.app.whatsapp.whatsappclone.model;

import com.app.whatsapp.whatsappclone.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;

public class Conversa {

    private String idRemetente;
    private String idDDestinatario;
    private String ultimaMensagem;
    private Usuario usuarioExibicao;

    public Conversa() {
    }

    public void salvar(){

        DatabaseReference database  = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("conversas");

        conversaRef.child(this.getIdRemetente())
                .child(this.getIdDDestinatario())
                .setValue(this);

    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDDestinatario() {
        return idDDestinatario;
    }

    public void setIdDDestinatario(String idDDestinatario) {
        this.idDDestinatario = idDDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuario) {
        this.usuarioExibicao = usuario;
    }
}
