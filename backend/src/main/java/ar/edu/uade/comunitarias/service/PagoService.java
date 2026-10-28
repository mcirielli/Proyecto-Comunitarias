package ar.edu.uade.comunitarias.service;
import ar.edu.uade.comunitarias.domain.*;
import ar.edu.uade.comunitarias.dto.*;
import ar.edu.uade.comunitarias.repository.*;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;
@Service @Profile("supabase") @ConditionalOnProperty(name="app.facturacion.enabled",havingValue="true")
public class PagoService {
    private final FacturaRepository facturas;private final PagoRepository pagos;private final FacturacionAcceso acceso;private final Clock clock;
    public PagoService(FacturaRepository f,PagoRepository p,FacturacionAcceso acceso,Clock clock){facturas=f;pagos=p;this.acceso=acceso;this.clock=clock;}
    @Transactional
    public PagoResponse registrar(RegistrarPagoRequest request,SessionResponse actor){
        acceso.operacion(actor);
        if(request==null || request.facturaId()==null || request.facturaId()<=0 || request.claveSolicitud()==null || request.medioPago()==null)throw new IllegalArgumentException("Pago incompleto");
        Factura.validarImporte(request.importe(),true);
        if(request.referencia()!=null && request.referencia().length()>100)throw new IllegalArgumentException("Referencia demasiado larga");
        // El lock de factura serializa saldo y reintentos sin modificar Pedido/State.
        var factura=facturas.bloquear(request.facturaId()).orElseThrow(()->new RecursoNoEncontradoException("Factura inexistente"));
        var existente=pagos.findByClaveSolicitud(request.claveSolicitud());
        if(existente.isPresent()){
            var pago=existente.get();
            if(!pago.coincide(factura.getId(),request.importe(),request.medioPago(),request.referencia()))throw new ReglaNegocioException("La clave ya identifica otro pago");
            return PagoResponse.de(pago);
        }
        if(factura.getEstado()==EstadoFactura.ANULADA)throw new ReglaNegocioException("Factura anulada");
        var acumulado=pagos.totalFactura(factura.getId()).add(request.importe());
        factura.registrarAcumulado(acumulado);
        var pago=pagos.saveAndFlush(new Pago(factura,request.claveSolicitud(),request.importe(),request.medioPago(),request.referencia(),clock.instant()));
        facturas.saveAndFlush(factura);
        return PagoResponse.de(pago);
    }
}
