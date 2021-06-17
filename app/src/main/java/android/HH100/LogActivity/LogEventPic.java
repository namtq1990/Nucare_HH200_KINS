package android.HH100.LogActivity;


import android.HH100.CameraUtil.Camera2Activity;
import android.HH100.CameraUtil.VideoActivity;
import android.HH100.DB.EventDBOper;
import android.HH100.IDspectrumActivity;
import android.HH100.MainActivity;
import android.HH100.R;
import android.HH100.RecActivity;
import android.HH100.Structure.EventData;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import static android.HH100.Structure.NcLibrary.SendEmail;
import static android.HH100.Structure.NcLibrary.hashMap;
import static android.app.Activity.RESULT_OK;

/**
 * Created by inseon.ahn on 2018-06-29.
 */

public class LogEventPic extends Fragment
{

    public static class LogGallery
    {
        public String file = "";
        public String fileName = "";
    }

   static Activity mAtvt;
    static android.HH100.Structure.EventData EventData = new EventData();
    int galleryType  = -1; //1 : photh, 2: video, 3:record
    AlertDialog.Builder dialogBuilder;
    ArrayList<LogGallery> eventGallery;
    public LogEventPic() {}
    String FolderPath;
    CHorizListView listGallery;
    TextView mMediaCnt;
    LogEventPicAdapter galleryAdt;
    String fileName = "";
    LinearLayout galleryLayout;
    LinearLayout main;
    RelativeLayout mainLayout;

    @SuppressLint("ValidFragment")
    public LogEventPic(Activity atvt, android.HH100.Structure.EventData item )
    {
        mAtvt = atvt;
        EventData = item;
    }

    public static LogEventPic newInstance(int index)
    {
        LogEventPic f = new LogEventPic();
        Bundle args = new Bundle(2);
        args.putInt("index", index);
        f.setArguments(args);
        return f;
    }



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.event_log_photo1, null);
        LogTabActivity.mPagerEvent.setPageScrollEnabled(false);

        MainActivity.ActionViewExcuteCheck = MainActivity.Activity_Mode.UN_EXCUTE_MODE;

        listGallery = (CHorizListView)view.findViewById(R.id.IDLIST_CHAT_GALLERY);
        mMediaCnt = (TextView) view.findViewById(R.id.EventLog_PhotoCount);
        ArrayList<String> arrGalleryData = new ArrayList<>();
        FolderPath = Environment.getExternalStorageDirectory() + "/" + EventDBOper.DB_FOLDER + "/";
        eventGallery = new ArrayList<LogGallery>();
        mainLayout = (RelativeLayout)view.findViewById(R.id.IDL_PIC_MAIN);
        galleryLayout = (LinearLayout)view.findViewById(R.id.IDL_PIC_GALLERY);
        main = (LinearLayout)view.findViewById(R.id.IDL_MAIN);

        mainLayout.setOnTouchListener(click);

        setGallery();

        galleryAdt = new LogEventPicAdapter(mAtvt, eventGallery, 600);
        listGallery.setAdapter(galleryAdt);

        mMediaCnt.setText(getResources().getString(R.string.photo_and_video) + " : " + eventGallery.size());
        galleryAdt.setOnListener(onGalleryClick);

        return view;
    }


    public void setGallery()
    {
        eventGallery =null;
        eventGallery = new ArrayList<LogGallery>();
        //사진
        if (EventData.PhotoFileName1 != null)
        {
            for (int i = 0; i < EventData.PhotoFileName1.size(); i++)
        {
            LogGallery item = new LogGallery();
            item.file = FolderPath+EventData.PhotoFileName1.get(i)+ ".png";
            item.fileName = EventData.PhotoFileName1.get(i);
            eventGallery.add(item);
        }
        }

        //동영상
        if (EventData.VedioFileName1 != null)
        {
            for (int i = 0; i < EventData.VedioFileName1.size(); i++)
            {
                LogGallery item = new LogGallery();
                item.file = FolderPath+EventData.VedioFileName1.get(i)+ ".mp4";
                item.fileName = EventData.VedioFileName1.get(i);
                eventGallery.add(item);
            }
        }

        //녹음
        if (EventData.RecodeFileName1 != null)
        {
            for (int i = 0; i < EventData.RecodeFileName1.size(); i++)
            {
                LogGallery item = new LogGallery();
                item.file = FolderPath+EventData.RecodeFileName1.get(i)+ ".amr";
                item.fileName = EventData.RecodeFileName1.get(i);
                eventGallery.add(item);
            }
        }
      /*  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        main.setWeightSum(1.0f);
        main.setLayoutParams(params);
*/
        mMediaCnt.setText(getResources().getString(R.string.photo_and_video) + " : " + ((eventGallery!=null)? eventGallery.size() : 0));
        if(eventGallery != null && eventGallery.size() != 0 )
        {
            LinearLayout.LayoutParams  params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,10);
            params.weight = 1.6f;
            galleryLayout.setLayoutParams(params);

            params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,10);
            params.weight = 8.4f;
            mainLayout.setLayoutParams(params);


            galleryAdt = new LogEventPicAdapter(mAtvt, eventGallery, 600);
            listGallery.setAdapter(galleryAdt);
            galleryAdt.setOnListener(onGalleryClick);
        }
        else
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight =10f;
            mainLayout.setLayoutParams(params);
            params.weight =0;
            galleryLayout.setLayoutParams(params);

        }


    }

    private LogEventPicAdapter.clickListener onGalleryClick = new LogEventPicAdapter.clickListener()
    {
        @Override
        public void onCellClick(String type, final int index, String file, final String name, final ArrayList<LogGallery> mArrFileUrl)
        {

            LogTabActivity.mPagerEvent.setPageScrollEnabled(false);
            Log.e("ahn",file);
            galleryType = -1;
            if(file.contains("EventP"))
            {
                galleryType = 1;
            }
           else if(file.contains("EventV"))
            {
                galleryType = 2;
            }
            else
            {
                galleryType = 3;
            }


            if (type.equals("click"))
            {
                MainActivity.ActionViewExcuteCheck = MainActivity.Activity_Mode.EXCUTE_MODE;
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(new File(file));
                switch (galleryType)
                {
                    case 1 :
                        intent.setDataAndType(uri, "image/*");
                        break;
                    case 2 :
                        intent.setDataAndType(uri, "video/*");
                        break;
                    case 3 :
                        intent.setDataAndType(uri, "audio/*");
                        break;
                }
                startActivity(intent);
                return;
            }

            if (type.equals("longClick"))
            {


                dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Dialog));
                dialogBuilder.setTitle(getResources().getString(R.string.delete));
                dialogBuilder.setMessage("'"+name+"'"+getResources().getString(R.string.delete_message));
                dialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        EventDBOper DB = new EventDBOper();
                        DB.OpenDB();
                        File f2d = null;
                      String updateFile = "";

                      switch (galleryType)
                        {
                            case 1 :
                                for(int i = 0; i<mArrFileUrl.size(); i++)
                                {
                                    if(mArrFileUrl.get(i).fileName.contains("EventP") && !mArrFileUrl.get(i).fileName.equals(name))
                                    {
                                        updateFile += mArrFileUrl.get(i).fileName+";";
                                    }
                                }

                                for(int k =0; k<EventData.PhotoFileName1.size(); k++)
                                {
                                    if(EventData.PhotoFileName1.get(k).equals(name))
                                    {
                                        f2d = new  File(FolderPath+name+".png");
                                        if(f2d.exists())
                                        {
                                            f2d.delete();
                                        }
                                        EventData.PhotoFileName1.remove(k);
                                    }
                                }

                             /*   f2d = new File(FolderPath+EventData.PhotoFileName1.get(index)+".png");
                                f2d.delete();*/

                              //  EventData.PhotoFileName1.remove(index);
                                DB.updateGallery(1,  EventData.Event_Number, updateFile);

                                break;
                            case 2 :
                                for(int i = 0; i<mArrFileUrl.size(); i++)
                                {
                                    if(mArrFileUrl.get(i).fileName.contains("EventV") && !mArrFileUrl.get(i).fileName.equals(name))
                                        updateFile += mArrFileUrl.get(i).fileName+";";
                                }
                                for(int k =0; k<EventData.VedioFileName1.size(); k++)
                                {
                                    if(EventData.VedioFileName1.get(k).equals(name))
                                    {
                                        f2d = new File(FolderPath+name+".mp4");
                                        if(f2d.exists())
                                        {
                                            f2d.delete();
                                        }
                                        EventData.VedioFileName1.remove(k);
                                    }
                                }
                                DB.updateGallery(2,  EventData.Event_Number, updateFile);
                                break;
                            case 3 :
                                for(int i = 0; i<mArrFileUrl.size(); i++)
                                {
                                    if(mArrFileUrl.get(i).fileName.contains("EventR") && !mArrFileUrl.get(i).fileName.equals(name))
                                        updateFile += mArrFileUrl.get(i).fileName+";";
                                }

                                for(int k =0; k<EventData.RecodeFileName1.size(); k++)
                                {
                                    if(EventData.RecodeFileName1.get(k).equals(name))
                                    {
                                        f2d = new  File(FolderPath+name+".amr");
                                        if(f2d.exists())
                                        {
                                            f2d.delete();
                                        }
                                        EventData.RecodeFileName1.remove(k);
                                    }
                                }

                    /*            f2d = new File(FolderPath+EventData.RecodeFileName1.get(index)+".amr");
                                f2d.delete();*/

                                DB.updateGallery(3,  EventData.Event_Number, updateFile);
                               // EventData.RecodeFileName1.remove(index);
                                break;
                        }

                        DB.EndDB();

                        galleryAdt.miSelIndex = -1;
                        //galleryAdt.mArrFileUrl.remove(index);
                        //galleryAdt.notifyDataSetChanged();

                        setGallery();
                    }
                });
                dialogBuilder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        galleryAdt.miSelIndex = -1;
                        galleryAdt.notifyDataSetChanged();

                    }
                    });
                dialogBuilder.setCancelable(false);
                dialogBuilder.show();
                return;
            }

        }
    };



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
       inflater.inflate(R.menu.event_log, menu);
    }

    //메뉴 선택
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        Intent intent = null;
        File file = null;
        fileName = "";
        switch (item.getItemId())
        {
            case R.id.Photo:
                MainActivity.ActionViewExcuteCheck = MainActivity.Activity_Mode.UN_EXCUTE_MODE;
               // intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileName = "EventP"+EventData.Event_Number + "_" + (EventData.PhotoFileName1.size()+1);
                file = new File(FolderPath+fileName + ".png");
                if (file.exists())
                {
                    fileName = "EventP"+EventData.Event_Number + "_" + (EventData.PhotoFileName1.size()+1)+"_";
                }
                file = new File(FolderPath, fileName + ".png");
               // intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
               // startActivityForResult(intent, 1);

                if(hashMap.get("photo")!=null)
                {
                    hashMap.remove("photo");
                }
                hashMap.put("photo", file.getAbsolutePath());

                //181026 수정
                intent = new Intent(getActivity(), Camera2Activity.class);
                intent.putExtra("file", file.getAbsolutePath());
                intent.putExtra("email", "F");
                intent.putExtra("hint", getResources().getString(R.string.camera_area));
                //프레이밍 영역 (전체 밝은 영역)으로 전체 화면 사용 여부
                intent.putExtra("hideBounds", true);
                //최대 허용 카메라 크기 (픽셀 수)
                intent.putExtra("maxPicturePixels", 3840 * 2160);
                startActivityForResult(intent, 1);

                break;

            case R.id.Video:
                MainActivity.ActionViewExcuteCheck = MainActivity.Activity_Mode.UN_EXCUTE_MODE;
                Intent Intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                fileName = "EventV"+EventData.Event_Number + "_" + (EventData.VedioFileName1.size()+1);
                file = new File(FolderPath+fileName + ".mp4");
                if (file.exists())
                {
                    fileName = "EventV"+EventData.Event_Number + "_" + (EventData.VedioFileName1.size()+1)+"_";
                }
                file = new File(FolderPath, fileName + ".mp4");
              //  Intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
              //  startActivityForResult(Intent, 2);

                if(hashMap.get("video")!=null)
                {
                    hashMap.remove("video");
                }
                hashMap.put("video", fileName);

                intent = new Intent(getActivity(), VideoActivity.class);
                intent.putExtra("path",fileName + ".mp4");
                startActivityForResult(intent, 2);
                break;

            case R.id.Recoder:

                intent = new Intent(mAtvt, RecActivity.class);
                intent.putExtra(IDspectrumActivity.Check.ListNumber, EventData.Event_Number);
                startActivityForResult(intent, 3);
                MainActivity.ACTIVITY_STATE = MainActivity.Activity_Mode.SOURCE_ID_RESULT_CAMERA;

                break;

/*            case R.id.reachback:
                SendEmail( false,mAtvt,  EventData.Event_Number);
                break;*/
        }
        return true;
    }



    //메뉴 결과값
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        String updateFile = "";
        File file = null;
        EventDBOper DB = new EventDBOper();
        DB.OpenDB();
            switch (requestCode) //requestCode 1: photo, 2: video, 3:recorde
            {
                case 1 :
                    if(hashMap.get("photo")!=null && !hashMap.get("photo").equals(""))
                    {
                        hashMap.remove("photo");
                        for(int i = 0; i<EventData.PhotoFileName1.size(); i++)
                        {
                            updateFile += EventData.PhotoFileName1.get(i)+";";
                        }
                        updateFile += fileName;
                        EventData.PhotoFileName1.add(fileName);
                        setGallery();
                    }
                   //DB.updateGallery(1,  EventData.Event_Number, updateFile);
                    break;
                case 2 :
                    if(hashMap.get("video")!=null && !hashMap.get("video").equals(""))
                    {
                        for (int i = 0; i < EventData.VedioFileName1.size(); i++) {
                            updateFile += EventData.VedioFileName1 + ";";
                        }
                        updateFile += fileName ;
                        EventData.VedioFileName1.add(fileName);
                        setGallery();
                        // DB.updateGallery(2,  EventData.Event_Number, updateFile);
                    }
                    break;
                case 3 :

                    ArrayList<String> recodeFileList = new ArrayList<String>();
                    recodeFileList = data.getStringArrayListExtra(IDspectrumActivity.Check.ListValue);
                    if(recodeFileList.size() != 0)
                    {
                        for (int i = 0; i < recodeFileList.size(); i++)
                        {
                            EventData.RecodeFileName1.add( recodeFileList.get(i));
                        }
                    }
                    if(EventData.RecodeFileName1.size() !=0)
                    {
                        for(int i = 0; i < EventData.RecodeFileName1.size(); i++)
                        {
                            File f2d = new  File(FolderPath+ EventData.RecodeFileName1.get(i)+".amr");
                            if(f2d.exists())
                            {
                                updateFile += EventData.RecodeFileName1.get(i)+";";
                            }
                        }
                     // updateFile += fileName;

                      setGallery();
                    // DB.updateGallery(3,  EventData.Event_Number, updateFile);
                    }

                    break;
            }
            if(!updateFile.equals(""))
            {
                DB.updateGallery(requestCode,  EventData.Event_Number, updateFile);
            }
            DB.EndDB();


    }


    //touch Listener
    public View.OnTouchListener click = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {
                switch (v.getId())
                {
                    case R.id.IDL_PIC_GALLERY:
                    case R.id.IDL_PIC_MAIN:
                        Log.e("ahn","main");
                        LogTabActivity.mPagerEvent.setPageScrollEnabled(true);
                        break;

                    default:
                        LogTabActivity.mPagerEvent.setPageScrollEnabled(false);
                        break;
                }
            }


            return false;
        }

    };


}


