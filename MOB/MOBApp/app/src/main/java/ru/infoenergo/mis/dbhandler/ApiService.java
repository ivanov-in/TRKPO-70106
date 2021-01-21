package ru.infoenergo.mis.dbhandler;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

interface ApiService {
    @Multipart
    @POST("add_file")
    Call<ResponseBody> postImage(@Part MultipartBody.Part file,
                                 @Part("upload") RequestBody name,
                                 @Query("p_id_task") String p_id_task,
                                 @Query("p_is_signed") String p_is_signed,
                                 @Query("p_paper") String p_paper,
                                 @Query("p_date_send_to_client") String p_date_send_to_client,
                                 @Query("p_email_client") String p_email_client,
                                 @Query("p_id_act") String p_id_act,
                                 @Query("p_npp") String p_npp);
}

