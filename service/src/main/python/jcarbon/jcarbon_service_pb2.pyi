import jcarbon.signal_pb2 as _signal_pb2
from google.protobuf.internal import containers as _containers
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class StartRequest(_message.Message):
    __slots__ = ("process_id", "period_millis")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    PERIOD_MILLIS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    period_millis: int
    def __init__(self, process_id: _Optional[int] = ..., period_millis: _Optional[int] = ...) -> None: ...

class StartResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class StopRequest(_message.Message):
    __slots__ = ("process_id",)
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    def __init__(self, process_id: _Optional[int] = ...) -> None: ...

class StopResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class ReadRequest(_message.Message):
    __slots__ = ("process_id", "signals")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    SIGNALS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    signals: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, process_id: _Optional[int] = ..., signals: _Optional[_Iterable[str]] = ...) -> None: ...

class ReadResponse(_message.Message):
    __slots__ = ("report",)
    REPORT_FIELD_NUMBER: _ClassVar[int]
    report: _signal_pb2.Report
    def __init__(self, report: _Optional[_Union[_signal_pb2.Report, _Mapping]] = ...) -> None: ...

class DumpRequest(_message.Message):
    __slots__ = ("process_id", "output_path", "signals")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_PATH_FIELD_NUMBER: _ClassVar[int]
    SIGNALS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    output_path: str
    signals: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, process_id: _Optional[int] = ..., output_path: _Optional[str] = ..., signals: _Optional[_Iterable[str]] = ...) -> None: ...

class DumpResponse(_message.Message):
    __slots__ = ("response",)
    RESPONSE_FIELD_NUMBER: _ClassVar[int]
    response: str
    def __init__(self, response: _Optional[str] = ...) -> None: ...

class PurgeRequest(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class PurgeResponse(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...
