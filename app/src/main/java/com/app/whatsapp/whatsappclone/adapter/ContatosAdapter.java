package com.app.whatsapp.whatsappclone.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.whatsapp.whatsappclone.R;
import com.app.whatsapp.whatsappclone.model.Usuario;
import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.MyViewHolder> {

    private List<Usuario> contatos;
    private Context context;

    public ContatosAdapter(List<Usuario> listaContatos, Context c) {
        this.contatos = listaContatos;
        this.context = c;
    }

    public List<Usuario> getContatos(){
        return this.contatos;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemLista = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_contatos,viewGroup,false);
        return new MyViewHolder(itemLista);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Usuario usuario = contatos.get(i);
        boolean cabecalho = usuario.getEmail().isEmpty();

        myViewHolder.nome.setText(usuario.getNome());
        myViewHolder.email.setText(usuario.getEmail());

        if (usuario.getFoto() != null){
            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with(context).load(uri).into(myViewHolder.foto);
        }else if (cabecalho){
            myViewHolder.foto.setImageResource(R.drawable.icone_grupo);
            myViewHolder.email.setVisibility(View.GONE);
        }
        else {
            myViewHolder.foto.setImageResource(R.drawable.padrao);
        }

    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome,email;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageViewFotoMembro);
            nome = itemView.findViewById(R.id.textNomeMembroSelecionado);
            email = itemView.findViewById(R.id.textEmailContato);
        }
    }
}
