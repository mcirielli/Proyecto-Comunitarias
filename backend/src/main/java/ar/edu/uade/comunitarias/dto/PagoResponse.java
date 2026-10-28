package ar.edu.uade.comunitarias.dto;
import ar.edu.uade.comunitarias.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record PagoResponse(Long id,Long facturaId,UUID claveSolicitud,Instant fechaPago,BigDecimal importe,MedioPago medioPago,String referencia) {
    public static PagoResponse de(Pago p){return new PagoResponse(p.getId(),p.getFactura().getId(),p.getClaveSolicitud(),p.getFechaPago(),p.getImporte(),p.getMedioPago(),p.getReferencia());}
}
