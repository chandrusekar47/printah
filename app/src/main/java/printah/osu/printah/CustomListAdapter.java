package printah.osu.printah;

/**
 * Created by sri on 1/21/18.
 */

        import android.app.Activity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.jcraft.jsch.HASH;

        import java.util.ArrayList;
        import java.util.HashMap;

public class CustomListAdapter extends ArrayAdapter<HashMap<String,String>> {

    private final Activity context;
    private final ArrayList<HashMap<String,String>> csvlist;

    public CustomListAdapter(Activity context, ArrayList<HashMap<String,String>> csvlist) {
        super(context, R.layout.printer_layout,csvlist);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.csvlist=csvlist;

    }



    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.printer_layout, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.building);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView extratxt = (TextView) rowView.findViewById(R.id.printer);
        TextView bruh = (TextView) rowView.findViewById(R.id.type);


        String line1 = csvlist.get(position).get("line1");
        txtTitle.setText(line1);
        String line2 = csvlist.get(position).get("line2");
        extratxt.setText(line2);
        String line3 = csvlist.get(position).get("line3");
        bruh.setText(line3);
       imageView.setImageResource(R.mipmap.free);
        return rowView;

    }
}

