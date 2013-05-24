  function toastSuccess(message){
    jSuccess(
      message,
      {
        autoHide : true, // added in v2.0
        clickOverlay : false, // added in v2.0
        MinWidth : 200,
        TimeShown : 3000,
        ShowTimeEffect : 200,
        HideTimeEffect : 200,
        LongTrip :20,
        HorizontalPosition : 'center',
        VerticalPosition : 'center',
        ShowOverlay : true,
        ColorOverlay : '#000',
        OpacityOverlay : 0.8,
        onClosed : function(){ // added in v2.0

        },
        onCompleted : function(){ // added in v2.0

        }
      });
  }
  function toastNotify(message){
    jNotify(
      message,
      {
        autoHide : true, // added in v2.0
        clickOverlay : false, // added in v2.0
        MinWidth : 200,
        TimeShown : 3000,
        ShowTimeEffect : 200,
        HideTimeEffect : 200,
        LongTrip :20,
        HorizontalPosition : 'center',
        VerticalPosition : 'center',
        ShowOverlay : true,
        ColorOverlay : '#000',
        OpacityOverlay : 0.8,
        onClosed : function(){ // added in v2.0

        },
        onCompleted : function(){ // added in v2.0

        }
      });
  }
  function toastFail(message){
    jError(
      message,
      {
        autoHide : true, // added in v2.0
        clickOverlay : false, // added in v2.0
        MinWidth : 200,
        TimeShown : 3000,
        ShowTimeEffect : 200,
        HideTimeEffect : 200,
        LongTrip :20,
        HorizontalPosition : 'center',
        VerticalPosition : 'center',
        ShowOverlay : true,
        ColorOverlay : '#000',
        OpacityOverlay : 0.8,
        onClosed : function(){ // added in v2.0

        },
        onCompleted : function(){ // added in v2.0

        }
      });
  }