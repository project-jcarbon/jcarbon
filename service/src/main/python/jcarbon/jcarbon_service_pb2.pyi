from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Optional as _Optional

DESCRIPTOR: _descriptor.FileDescriptor

class StartRequest(_message.Message):
    __slots__ = ("process_id", "period_millis")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    PERIOD_MILLIS_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    period_millis: int
    def __init__(self, process_id: _Optional[int] = ..., period_millis: _Optional[int] = ...) -> None: ...

class StartResponse(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class StopRequest(_message.Message):
    __slots__ = ("process_id",)
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    def __init__(self, process_id: _Optional[int] = ...) -> None: ...

class StopResponse(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class DumpRequest(_message.Message):
    __slots__ = ("process_id", "output_path")
    PROCESS_ID_FIELD_NUMBER: _ClassVar[int]
    OUTPUT_PATH_FIELD_NUMBER: _ClassVar[int]
    process_id: int
    output_path: str
    def __init__(self, process_id: _Optional[int] = ..., output_path: _Optional[str] = ...) -> None: ...

class DumpResponse(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...
