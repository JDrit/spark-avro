/*
 * Copyright 2014 Databricks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.databricks.spark.avro.avroRelation2

import com.databricks.spark.avro.AvroSaver
import org.apache.avro.generic.GenericRecord
import org.apache.avro.mapred.AvroKey
import org.apache.avro.mapreduce.AvroKeyOutputFormat
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.NullWritable
import org.apache.hadoop.mapreduce.{RecordWriter, TaskAttemptContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.sources.OutputWriter
import org.apache.spark.sql.types.StructType

// NOTE: This class is instantiated and used on executor side only, no need to be serializable.
private[avro] class AvroOutputWriter(path: String,
                                    context: TaskAttemptContext,
                                    schema: StructType,
                                    recordName: String,
                                    recordNamespace: String) extends OutputWriter  {

  private lazy val converter = AvroSaver.createConverter(schema, recordName, recordNamespace)

  private val recordWriter: RecordWriter[AvroKey[GenericRecord], NullWritable] =
    new AvroKeyOutputFormat[GenericRecord]().getRecordWriter(context)

  override def write(row: Row): Unit = {
    val key = new AvroKey(converter(row).asInstanceOf[GenericRecord])
    recordWriter.write(key, NullWritable.get())
  }

  override def close(): Unit = recordWriter.close(context)
}