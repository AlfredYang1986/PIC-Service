package bmutil.ExcelReader

import bmutil.ExcelReader.JavaBean.RawData
import com.pharbers.aqll.common.alFileHandler.alExcelOpt.scala.alExcelDataParser

/**
  * Created by yym on 7/27/17.
  */
object RawDataReader {
    def read(path: String) : List[RawData]={
        val en_rawData="bmutil/ExcelReader/XMLFile/en_RawData.xml"
        val ch_rawData="bmutil/ExcelReader/XMLFile/ch_RawData.xml"
        val data=new alExcelDataParser(new RawData,en_rawData,ch_rawData)
        data.prase("bmutil/ExcelReader/ExcelFile/sampleData.xlsx")("")
        data.data.toList.asInstanceOf[List[RawData]]
    }
}
