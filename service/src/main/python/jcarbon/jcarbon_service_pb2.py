# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: jcarbon_service.proto
# Protobuf Python Version: 4.25.1
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
from google.protobuf.internal import builder as _builder
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()


import jcarbon.signal_pb2 as signal__pb2


DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x15jcarbon_service.proto\x12\x0fjcarbon.service\x1a\x0csignal.proto\"d\n\x0cStartRequest\x12\x17\n\nprocess_id\x18\x01 \x01(\x04H\x00\x88\x01\x01\x12\x1a\n\rperiod_millis\x18\x02 \x01(\rH\x01\x88\x01\x01\x42\r\n\x0b_process_idB\x10\n\x0e_period_millis\"3\n\rStartResponse\x12\x15\n\x08response\x18\x01 \x01(\tH\x00\x88\x01\x01\x42\x0b\n\t_response\"5\n\x0bStopRequest\x12\x17\n\nprocess_id\x18\x01 \x01(\x04H\x00\x88\x01\x01\x42\r\n\x0b_process_id\"2\n\x0cStopResponse\x12\x15\n\x08response\x18\x01 \x01(\tH\x00\x88\x01\x01\x42\x0b\n\t_response\"F\n\x0bReadRequest\x12\x17\n\nprocess_id\x18\x01 \x01(\x04H\x00\x88\x01\x01\x12\x0f\n\x07signals\x18\x02 \x03(\tB\r\n\x0b_process_id\"F\n\x0cReadResponse\x12+\n\x06report\x18\x01 \x01(\x0b\x32\x16.jcarbon.signal.ReportH\x00\x88\x01\x01\x42\t\n\x07_report\"p\n\x0b\x44umpRequest\x12\x17\n\nprocess_id\x18\x01 \x01(\x04H\x00\x88\x01\x01\x12\x18\n\x0boutput_path\x18\x02 \x01(\tH\x01\x88\x01\x01\x12\x0f\n\x07signals\x18\x03 \x03(\tB\r\n\x0b_process_idB\x0e\n\x0c_output_path\"2\n\x0c\x44umpResponse\x12\x15\n\x08response\x18\x01 \x01(\tH\x00\x88\x01\x01\x42\x0b\n\t_response\"\x0e\n\x0cPurgeRequest\"\x0f\n\rPurgeResponse2\xf9\x02\n\x0eJCarbonService\x12H\n\x05Start\x12\x1d.jcarbon.service.StartRequest\x1a\x1e.jcarbon.service.StartResponse\"\x00\x12\x45\n\x04Stop\x12\x1c.jcarbon.service.StopRequest\x1a\x1d.jcarbon.service.StopResponse\"\x00\x12\x45\n\x04\x44ump\x12\x1c.jcarbon.service.DumpRequest\x1a\x1d.jcarbon.service.DumpResponse\"\x00\x12\x45\n\x04Read\x12\x1c.jcarbon.service.ReadRequest\x1a\x1d.jcarbon.service.ReadResponse\"\x00\x12H\n\x05Purge\x12\x1d.jcarbon.service.PurgeRequest\x1a\x1e.jcarbon.service.PurgeResponse\"\x00\x42\x13\n\x0fjcarbon.serviceP\x01\x62\x06proto3')

_globals = globals()
_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, _globals)
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'jcarbon_service_pb2', _globals)
if _descriptor._USE_C_DESCRIPTORS == False:
  _globals['DESCRIPTOR']._options = None
  _globals['DESCRIPTOR']._serialized_options = b'\n\017jcarbon.serviceP\001'
  _globals['_STARTREQUEST']._serialized_start=56
  _globals['_STARTREQUEST']._serialized_end=156
  _globals['_STARTRESPONSE']._serialized_start=158
  _globals['_STARTRESPONSE']._serialized_end=209
  _globals['_STOPREQUEST']._serialized_start=211
  _globals['_STOPREQUEST']._serialized_end=264
  _globals['_STOPRESPONSE']._serialized_start=266
  _globals['_STOPRESPONSE']._serialized_end=316
  _globals['_READREQUEST']._serialized_start=318
  _globals['_READREQUEST']._serialized_end=388
  _globals['_READRESPONSE']._serialized_start=390
  _globals['_READRESPONSE']._serialized_end=460
  _globals['_DUMPREQUEST']._serialized_start=462
  _globals['_DUMPREQUEST']._serialized_end=574
  _globals['_DUMPRESPONSE']._serialized_start=576
  _globals['_DUMPRESPONSE']._serialized_end=626
  _globals['_PURGEREQUEST']._serialized_start=628
  _globals['_PURGEREQUEST']._serialized_end=642
  _globals['_PURGERESPONSE']._serialized_start=644
  _globals['_PURGERESPONSE']._serialized_end=659
  _globals['_JCARBONSERVICE']._serialized_start=662
  _globals['_JCARBONSERVICE']._serialized_end=1039
# @@protoc_insertion_point(module_scope)
