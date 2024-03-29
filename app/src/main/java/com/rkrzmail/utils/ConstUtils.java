package com.rkrzmail.utils;

import android.os.Environment;

import com.naa.data.UtilityAndroid;
import com.rkrzmail.oto.AppActivity;

public class ConstUtils {

    public static String PRINT_BUKTI_BAYAR(String params, boolean isCheckin) {
        return isCheckin ?
                "https://otomotives.com/internaldev/report/bukti_bayar/" + params :
                "https://otomotives.com/internaldev/report/bukti_beli_part/" + params;
    }

    public static String CETAK_EXCEL(String CID, String Entry, String tglAwal, String tglAkhir) {
        return "https://otomotives.com/internaldev/laporan_kinerja?cid="+CID+"&nama_laporan="+Entry+"&periode_awal="+tglAwal+"&periode_akhir="+tglAkhir;
    }

    public static int DAYS(long milliseconds) {
        return (int) (milliseconds / (1000 * 60 * 60 * 24));
    }

    public static final int PICK_IMAGE_CAMERA = 1001;
    public static final int PICK_IMAGE_GALLERY = 1002;
    public static final String DATE_DEFAULT_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
    public static final long ONEDAY = 86400000;
    public static final String EXTERNAL_DIR_OTO = Environment.getExternalStorageDirectory() + "/Otomotives";
    //ARGUMENTS PARENT
    public static final String ERROR_INFO = "Terjadi Kesalahan Silahkan Coba Kembali";
    public static final String TUGAS_PART_TERSEDIA = "TUGAS_PART_TERSEDIA";
    public static final String TUGAS_PART_PERMINTAAN = "TUGAS_PART_PERMINTAAN";
    public static final String TUGAS_PART_BATAL = "TUGAS_PART_BATAL";
    public static final String TUGAS_PART_KOSONG = "TUGAS_PART_KOSONG";
    public static final String RINCIAN_LAYANAN = "Rincian Layanan";
    public static final String RINCIAN_DP = "Rincian DP";
    public static final String RINCIAN_JUAL_PART = "Rincian Jual Part";
    //CONST STRING
    public static final String BENGKEL = "BENGKEL";
    public static final String BATAL_PART = "BATAL_PART";
    public static final String USULAN_MEKANIK = "USULAN_MEKANIK";
    public static final String TAMBAH_PART = "TAMBAH_PART";
    public static final String ID = "ID";
    public static final String TOTAL_BIAYA = "TOTAL_BIAYA";
    public static final String ESTIMASI_WAKTU = "ESTIMASI_WAKTU";
    public static final String TAMBAH = "TAMBAH";
    public static final String TIDAK_MENUNGGU = "TIDAK_MENUNGGU";
    public static final String MENUNGGU = "MENUNGGU";
    public static final String MASTER_PART = "MASTER_PART";
    public static final String PARTS_UPPERCASE = "PARTS";
    public static final String JASA_LAIN = "JASA_LAIN";
    public static final String BATAL = "BATAL";
    public static final String ADD = "ADD";
    public static final String EDIT = "EDIT";
    public static final String CARI_PART = "CARI_PART";
    public static final String LANJUT = "LANJUT";
    public static final String TAG_PART_KELUAR = "PART_KELUAR___";
    public static final String ATUR = "ATUR";
    public static final String ALL = "ALL";
    public static final String DETAIL = "DETAIL";
    public static final String RP = "Rp. ";
    public static final String DATA = "DATA";
    public static final String PART = "part";
    public static final String PENYESUAIAN = "PENYESUAIAN";
    public static final String RUANG_PART = "RUANG PART";
    public static final String PART_WAJIB = "PART WAJIB";
    public static final String GARANSI_PART = "GARANSI PART";
    //FLAG CARI PART
    public static final String CARI_PART_LOKASI = "cari_part_lokasi";
    public static final String CARI_PART_BENGKEL = "bengkel";
    public static final String CARI_PART_OTOMOTIVES = "global";
    public static final String CARI_PART_TERALOKASIKAN = "TERALOKASI";
    public static final String CARI_PART_CLAIM = "CLAIM";
    //REQUEST CODE
    public static final int REQUEST_KONFIRMASI = 4;
    public static final int REQUEST_WA = 5;
    public static final int REQUEST_PART_KOSONG = 9;
    public static final int REQUEST_TUGAS_PART = 10;
    public static final int REQUEST_DISCOUNT = 11;
    public static final int REQUEST_CARI_PART = 12;
    public static final int REQUEST_DETAIL = 13;
    public static final int REQUEST_JUMLAH_PART_KELUAR = 14;
    public static final int REQUEST_BARCODE = 15;
    public static final int REQUEST_DAFTAR_PART_KELUAR = 16;
    public static final int REQUEST_PART = 17;
    public static final int REQUEST_MEKANIK = 18;
    public static final int REQUEST_LOKASI = 19;
    public static final int REQUEST_PART_KELUAR = 20;
    public static final int REQUEST_PART_KEMBALI = 21;
    public static final int REQUEST_OPNAME = 22;
    public static final int REQUEST_PENYESUAIAN = 23;
    public static final int REQEST_DAFTAR_JUAL = 24;
    public static final int REQUEST_ATUR_LOKASI = 25;
    public static final int REQUEST_CHECKIN = 26;
    public static final int REQUEST_HISTORY = 27;
    public static final int REQUEST_NEW_CS = 28;
    public static final int REQUEST_JASA_LAIN = 29;
    public static final int REQUEST_JASA_BERKALA = 30;
    public static final int REQUEST_PART_BERKALA = 31;
    public static final int REQUEST_PART_EXTERNAL = 32;
    public static final int REQUEST_JASA_EXTERNAL = 33;
    public static final int REQUEST_BATAL = 34;
    public static final int REQUEST_HARGA_PART = 35;
    public static final int REQUEST_BIAYA = 36;
    public static final int REQUEST_CODE_SIGN = 37;
    public static final int PERMISSION_REQUEST_CODE = 38;
    public static final int REQUEST_LAYANAN = 39;
    public static final int REQUEST_TAMBAH_PART_JASA_LAIN = 40;
    public static final int REQUEST_STATUS = 41;
    public static final int REQUEST_DISC_SPOT = 42;
    public static final int REQUEST_CONTACT = 45;
    public static final int REQUEST_REKENING = 46;
    public static final int REQUEST_FOTO_PART = 47;
    public static final int REQUEST_FOTO_STNK = 48;
    public static final int REQUEST_FOTO_KTP = 49;
    public static final int REQUEST_SARAN = 50;
    public static final int REQUEST_FOTO = 51;
}
