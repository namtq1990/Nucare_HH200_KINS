package android.HH100.LogActivity;

import android.HH100.R;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.HH100.LogActivity.LogEventPic.LogGallery;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by inseon.ahn on 2018. 7. 2..
 */
public class LogEventPicAdapter extends BaseAdapter
{

	private Activity mCtxt;
	public static ArrayList<LogGallery> mArrFileUrl;
	public int miHeight = 0;
	public static int miSelIndex = -1;
	public String selectImg = "";

	// 리스트 아이템 구조체
	public class ListHolder
	{
		public ImageView file,video;
		public RelativeLayout layout, imgLayout;
		public TextView fileName;
		public int idx;
	}
	private ListHolder mHolder;

	public void setOnListener(clickListener listener)
	{
		mListener = listener;
	}
	public interface clickListener
	{
		void onCellClick(String type, int index, String file, String name, ArrayList<LogGallery> mArrFileUrl);
	}
	private clickListener mListener;

	int preTouchX = 0;
	int currentTouchX = 0;

	public LogEventPicAdapter(Activity context, ArrayList<LogGallery> arrFileUrl, int listHeight)
	{
		mCtxt = context;

		miHeight = listHeight;
		mArrFileUrl = new ArrayList<>();

		if(arrFileUrl != null)
			mArrFileUrl.addAll(arrFileUrl);
	}


	@Override
	public int getCount() {
		return mArrFileUrl.size();
	}

	@Override
	public Object getItem(int i) {
		return null;
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup)
	{

		if(view == null)
		{
			LayoutInflater inflater = (LayoutInflater) mCtxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.event_log_gallery, viewGroup, false);

			mHolder = new ListHolder();
			mHolder.file = (ImageView) view.findViewById(R.id.IDIMG_GALLERY_FILE);
			mHolder.video = (ImageView) view.findViewById(R.id.IDIMG_PLAY_VIDEO);
			mHolder.layout = (RelativeLayout)view.findViewById(R.id.ID_GALLERY_MAIN);
			mHolder.imgLayout = (RelativeLayout)view.findViewById(R.id.IDL_GALLERY_IMG);
			mHolder.fileName = (TextView)view.findViewById(R.id.ID_GALLERY_FILE_NAME);
			view.setTag(mHolder);
		}
		else
		{
			mHolder = (ListHolder) view.getTag();
		}

		mHolder.idx = i;
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(480, 395);
		param.setMargins(15, 0, 15, 0);
		mHolder.file.setLayoutParams(param);

		RelativeLayout.LayoutParams textParam = new RelativeLayout.LayoutParams(480, 10);
		textParam.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
		mHolder.video.setVisibility(View.INVISIBLE);
		mHolder.fileName.setText(mArrFileUrl.get(i).fileName);

		if(mArrFileUrl.get(i).file.contains(".amr"))
		{
			Glide.with(mCtxt).load(R.drawable.rec).into(mHolder.file);
		}
		else
		{
			if(mArrFileUrl.get(i).file.contains(".mp4"))
			{
				mHolder.video.setVisibility(View.VISIBLE);
			}
			Glide.with(mCtxt).asBitmap().load(mArrFileUrl.get(i).file).thumbnail(0.1f).into(mHolder.file);
		}

		mHolder.file.setBackgroundResource(R.drawable.photo_frame);

		if(miSelIndex == i)
		{
			mHolder.file.setBackgroundResource(R.drawable.photo_frame_select);
		}


		mHolder.imgLayout.setTag(mHolder);
		mHolder.layout.setTag(mHolder);

		mHolder.layout.setOnTouchListener(touch);
		mHolder.imgLayout.setOnTouchListener(touch);
		mHolder.imgLayout.setOnLongClickListener(longClick);


		return view;
	}

	/*
		터치이벤트로 처리
		preTouchX == currentTouchX : gallery항목을 누른걸로 추정하여 클릭 메서드 호출
		preTouchX != currentTouchX  : gallery스크롤로 추정하여 touch 메서드 호출
	 */
	private View.OnTouchListener touch = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent event)
		{

			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				preTouchX = (int)event.getX();
				return false;
			}
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				currentTouchX = (int)event.getX();
			}

			ListHolder holder = (ListHolder) view.getTag();
			if(preTouchX != currentTouchX )
			{
				mListener.onCellClick("touch" ,  holder.idx, mArrFileUrl.get(holder.idx).file, mArrFileUrl.get(holder.idx).fileName, mArrFileUrl);
			}
			else
			{
				mListener.onCellClick("click" ,  holder.idx, mArrFileUrl.get(holder.idx).file, mArrFileUrl.get(holder.idx).fileName, mArrFileUrl);
			}
			return false;
		}

	};

	// click Listener
	private View.OnLongClickListener longClick = new View.OnLongClickListener()
	{
		@Override
		public boolean onLongClick(View view)
		{
			ListHolder holder = (ListHolder) view.getTag();
			if(view.getId() ==R.id.IDL_GALLERY_IMG )
			{
				miSelIndex = holder.idx;
				notifyDataSetChanged();

				mListener.onCellClick("longClick" , holder.idx, mArrFileUrl.get(holder.idx).file, mArrFileUrl.get(holder.idx).fileName, mArrFileUrl);
			}
			return false;
		}

	};
}
