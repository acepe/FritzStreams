package de.acepe.fritzstreams.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.acepe.fritzstreams.R;

public class IconLinkButton extends LinearLayout {

    ImageButton imageButton;
    TextView category;
    TextView genre;

    public IconLinkButton(Context context) {
        super(context);
        init(context, null);
    }

    public IconLinkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IconLinkButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.icon_link_button, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        category = (TextView) findViewById(R.id.tvStreamCategory);
        genre = (TextView) findViewById(R.id.tvStreamGenre);
        imageButton = (ImageButton) findViewById(R.id.ibDownload);
        setOnTouchListener(oclClicked);

        assignAttributes(context, attrs);
    }

    private void assignAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IconLinkButton, 0, 0);

            String categoryText = "";
            String genreText = "";

            try {
                categoryText = attributes.getString(R.styleable.IconLinkButton_categoryText);
                genreText = attributes.getString(R.styleable.IconLinkButton_genreText);
            } catch (Exception e) {
                Log.e("IconLinkButton", "There was an error loading attributes.");
            } finally {
                attributes.recycle();
            }

            setCategoryText(categoryText);
            setGenreText(genreText);
        }
    }

    private OnTouchListener oclClicked = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.i("ilb", "ontouch");
            motionEvent.setLocation(0.0f, 0.0f);
            imageButton.dispatchTouchEvent(motionEvent);
            return true;
        }
    };

    public void setCategoryText(String text) {
        category.setText(text);
    }

    public void setCategoryText(int resId) {
        category.setText(resId);
    }

    public void setGenreText(String text) {
        genre.setText(text);
    }

    public void setGenreText(int resId) {
        genre.setText(resId);
    }

    public void setOnClickListener(OnClickListener listener) {
        imageButton.setOnClickListener(listener);
    }

}