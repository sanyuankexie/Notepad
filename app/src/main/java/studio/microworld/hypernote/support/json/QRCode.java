package studio.microworld.hypernote.support.json;

import com.google.gson.annotations.SerializedName;


/**
 * Created by Mr.小世界 on 2018/9/14.
 */

public final class QRCode
{
    @SerializedName(NOTE_URL_PROTOCOL)
    public String uuid;

    public QRCode(String uuid)
    {
        this.uuid = uuid;
    }

    public static final String NOTE_URL_PROTOCOL = "note_id";

}
