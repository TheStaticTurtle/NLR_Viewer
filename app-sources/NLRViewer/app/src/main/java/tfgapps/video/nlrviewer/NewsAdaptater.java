package tfgapps.video.nlrviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class News {
    public String title;
    public String date;
    public View.OnClickListener onNewsClick;

    public News(String title, String date, View.OnClickListener click) {
        this.title = title;
        this.date = date;
        this.onNewsClick = click;
    }
}

public class NewsAdaptater extends ArrayAdapter<News> {

    //tweets est la liste des models à afficher
    public NewsAdaptater(Context context, List<News> f) {
        super(context, 0, f);
    }

    private class NewsViewHolder{
        private TextView title;
        private TextView date;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_news,parent, false);
        }

        NewsViewHolder viewHolder = (NewsViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new NewsViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.rowComicListTitle);
            viewHolder.date = (TextView) convertView.findViewById(R.id.rowNewsDate);
            convertView.setTag(viewHolder);
        }

        //getItem(position) va récupérer l'item [position] de la List<News> tweets
        News ep = getItem(position);

        //il ne reste plus qu'à remplir notre vue
        viewHolder.title.setText(ep.title);
        viewHolder.date.setText(ep.date);
        viewHolder.title.setOnClickListener(ep.onNewsClick);
        viewHolder.date.setOnClickListener(ep.onNewsClick);

        return convertView;
    }

}
